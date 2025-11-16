/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package sap.commerce.toolset.hac.exec.http

import com.intellij.credentialStore.Credentials
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import org.apache.http.HttpHeaders
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.HttpVersion
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.BasicHttpClientConnectionManager
import org.apache.http.message.BasicHttpResponse
import org.apache.http.message.BasicNameValuePair
import org.apache.http.message.BasicStatusLine
import org.apache.http.ssl.SSLContexts
import org.jsoup.Connection
import org.jsoup.Jsoup
import sap.commerce.toolset.exec.ExecConstants
import sap.commerce.toolset.exec.context.ReplicaContext
import sap.commerce.toolset.hac.auth.HacManualAuthenticator
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.AuthMode
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.hac.exec.settings.state.ProxyAuthMode
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import kotlin.io.encoding.Base64

@Service(Service.Level.PROJECT)
class HacHttpClient(private val project: Project) {

    suspend fun testConnection(
        settings: HacConnectionSettingsState,
        username: String,
        password: String,
        proxyUsername: String? = null,
        proxyPassword: String? = null
    ): HacHttpAuthResult {
        val authContextCache = AuthContextCache.getInstance(project)
        val authContextKey = authContextCache.getKey(settings)

        val proxyCredentials: Credentials? = if (proxyUsername?.isNotBlank() ?: false && proxyPassword?.isNotBlank() ?: false)
            Credentials(proxyUsername, proxyPassword)
        else null

        return authenticate(settings, authContextKey, username, password, proxyCredentials)
            .also { authContextCache.authContexts.remove(authContextKey) }
    }

    suspend fun post(
        actionUrl: String,
        params: Collection<BasicNameValuePair>,
        canReLoginIfNeeded: Boolean,
        timeout: Int,
        settings: HacConnectionSettingsState,
        replicaContext: ReplicaContext?,
    ): HttpResponse {
        val authContextCache = AuthContextCache.getInstance(project)
        val authContextKey = authContextCache.getKey(settings, replicaContext)
        var authContext = authContextCache.authContexts[authContextKey]
        val sessionCookieName = getSessionCookieName(settings)
        var prefilledCsrfToken: String? = null
        val execConnectionService = HacExecConnectionService.getInstance(project)

        if (authContext == null || !authContext.cookies.containsKey(sessionCookieName)) {
            if (settings.authMode == AuthMode.MANUAL) {
                val authContext = HacManualAuthenticator.getService(project)
                    .authenticate(settings)
                    ?.takeIf { it.isValid(sessionCookieName) }
                    ?: return createErrorResponse("Unable to find cookie $sessionCookieName")

                authContextCache.authContexts[authContextKey] = authContext.toAuthContext()
                prefilledCsrfToken = authContext.csrfToken
            } else {
                val credentials = execConnectionService.getCredentials(settings)
                val proxyCredentials = if (settings.proxyAuthMode == ProxyAuthMode.BASIC) execConnectionService.getProxyCredentials(settings)
                else null
                val username = credentials.userName ?: ""
                val password = credentials.getPasswordAsString() ?: ""
                val authResult = authenticate(settings, authContextKey, username, password, proxyCredentials, replicaContext)

                if (authResult is HacHttpAuthResult.Error) {
                    return createErrorResponse(authResult.message)
                }
            }
        }

        authContext = authContextCache.authContexts[authContextKey]
            ?: return createErrorResponse("Unable to authenticate request.")

        val sessionId = authContext.cookies[sessionCookieName]
        val generatedURL = settings.generatedURL
        val csrfToken = prefilledCsrfToken
            ?: getCsrfToken(generatedURL, settings, authContext)

        if (csrfToken == null) {
            authContextCache.authContexts.remove(authContextKey)

            if (canReLoginIfNeeded) {
                return post(actionUrl, params, false, timeout, settings, replicaContext)
            }
            return createErrorResponse("Unable to obtain csrfToken for sessionId=$sessionId")
        }

        val client = createAllowAllClient(timeout)
            ?: return createErrorResponse("Unable to create HttpClient")


        val post = HttpPost(actionUrl).apply {
            authContext.headers.forEach { setHeader(it.key, it.value) }

            setHeader("User-Agent", HttpHeaders.USER_AGENT)
            setHeader("X-CSRF-TOKEN", csrfToken)
            setHeader("Cookie", authContext.cookies.entries.joinToString("; ") { it.key + "=" + it.value })
            setHeader("Accept", "application/json")
            setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            setHeader("Sec-Fetch-Dest", "empty")
            setHeader("Sec-Fetch-Mode", "cors")
            setHeader("Sec-Fetch-Site", "same-origin")
        }

        val response: HttpResponse
        try {
            post.entity = UrlEncodedFormEntity(params, StandardCharsets.UTF_8)
            response = client.execute(post)
        } catch (e: IOException) {
            thisLogger().warn(e.message, e)
            return createErrorResponse(e.message)
        }

        val statusCode = response.statusLine.statusCode
        val needsLogin = when (statusCode) {
            HttpStatus.SC_FORBIDDEN, HttpStatus.SC_METHOD_NOT_ALLOWED -> true
            HttpStatus.SC_MOVED_TEMPORARILY -> {
                val location = response.getFirstHeader("Location")
                location != null && location.value.contains("login")
            }

            else -> false
        }
        if (needsLogin) {
            authContextCache.authContexts.remove(authContextKey)
            if (canReLoginIfNeeded) {
                return post(actionUrl, params, false, timeout, settings, replicaContext)
            }
        }
        return response
    }

    private suspend fun authenticate(
        settings: HacConnectionSettingsState,
        authContextKey: String,
        username: String,
        password: String,
        proxyCredentials: Credentials? = null,
        replicaContext: ReplicaContext? = null
    ): HacHttpAuthResult {
        val hostHacURL = settings.generatedURL
        val authContextCache = AuthContextCache.getInstance(project)
        val authContext = authContextCache.authContexts.computeIfAbsent(authContextKey) { AuthContext(
            headers = proxyCredentials.asHeaders()
        ) }
        retrieveCookies(hostHacURL, settings, replicaContext, authContext)

        val sessionCookieName = getSessionCookieName(settings)
        val cookies = authContext.cookies
        cookies[sessionCookieName]
            ?: return HacHttpAuthResult.Error(hostHacURL, "Unable to obtain sessionId for $hostHacURL")

        val csrfToken = getCsrfToken(hostHacURL, settings, authContext)
            ?: return HacHttpAuthResult.Error(hostHacURL, "Unable to obtain csrfToken for $hostHacURL")

        val params = listOf(
            BasicNameValuePair("j_username", username),
            BasicNameValuePair("j_password", password),
            BasicNameValuePair("_csrf", csrfToken),
        )
        val loginURL = "$hostHacURL/j_spring_security_check"
        val response = post(loginURL, params, false, settings.timeout, settings, replicaContext)
        val statusCode = response.statusLine.statusCode
        if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
            val location = response.getFirstHeader("Location")
            if (location != null && location.value.contains("login_error")) {
                return HacHttpAuthResult.Error(hostHacURL, "Wrong username/password. Set your credentials in [y] tool window.")
            }
        }

        return CookieParser.getInstance().getSpecialCookie(response.allHeaders)
            ?.let { newSessionId ->
                authContextCache.authContexts[authContextKey]?.cookies?.let { it[sessionCookieName] = newSessionId }
                HacHttpAuthResult.Success(hostHacURL)
            }
            ?: HacHttpAuthResult.Error(hostHacURL, buildString {
                append("HTTP ")
                append(statusCode)
                append(" ")

                when (statusCode) {
                    HttpURLConnection.HTTP_OK -> append("Unable to obtain sessionId from response")
                    HttpURLConnection.HTTP_MOVED_TEMP -> append(response.getFirstHeader("Location"))
                    else -> append(response.statusLine.reasonPhrase)
                }
            })
    }

    private fun createAllowAllClient(timeout: Int): CloseableHttpClient? {
        val sslContext: SSLContext
        try {
            sslContext = SSLContexts.custom()
                .loadTrustMaterial(null) { _, _ -> true }
                .build()
        } catch (e: Exception) {
            thisLogger().warn(e.message, e)
            return null
        }

        return HttpClients.custom()
            .setConnectionManager(
                BasicHttpClientConnectionManager(
                    RegistryBuilder.create<ConnectionSocketFactory>()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                        .build()
                )
            )
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .build()
            )
            .build()
    }

    private fun createErrorResponse(reasonPhrase: String?) = BasicHttpResponse(
        BasicStatusLine(
            HttpVersion.HTTP_1_1,
            HttpStatus.SC_SERVICE_UNAVAILABLE,
            reasonPhrase
        )
    )

    private fun retrieveCookies(
        hacURL: String,
        settings: HacConnectionSettingsState,
        replicaContext: ReplicaContext?,
        authContext: AuthContext
    ) {
        authContext.cookies.clear()

        val res = getResponseForUrl(hacURL, settings, replicaContext, authContext)
            ?: return

        authContext.cookies.putAll(res.cookies())

        if (replicaContext != null) {
            authContext.cookies[replicaContext.cookieName] = replicaContext.replicaCookie
        }
    }

    private fun getSessionCookieName(settings: HacConnectionSettingsState) = settings.sessionCookieName
        .takeIf { it.isNotBlank() }
        ?: ExecConstants.DEFAULT_SESSION_COOKIE_NAME

    private fun getResponseForUrl(
        hacURL: String,
        settings: HacConnectionSettingsState,
        replicaContext: ReplicaContext?,
        authContext: AuthContext
    ): Connection.Response? {
        try {
            val connection = connect(hacURL, settings.sslProtocol)
                .headers(authContext.headers)
                //.cookies(authContext.cookies)

            if (replicaContext != null) {
                connection.cookie(replicaContext.cookieName, replicaContext.replicaCookie)
            }

            return connection
                .method(Connection.Method.GET)
                .execute()
        } catch (_: ConnectException) {
            return null
        } catch (e: Exception) {
            thisLogger().warn(e.message, e)
            return null
        }
    }

    private fun getCsrfToken(
        hacURL: String,
        settings: HacConnectionSettingsState,
        authContext: AuthContext
    ): String? = try {
        connect(hacURL, settings.sslProtocol)
            .cookies(authContext.cookies)
            .headers(authContext.headers)
            .get()
            .select("meta[name=_csrf]")
            .attr("content")
    } catch (e: Exception) {
        thisLogger().warn(e.message, e)
        null
    }

    @Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
    private fun connect(url: String, sslProtocol: String): Connection {
        val trustAllCerts = arrayOf(TrustAllX509TrustManager)

        val sc = SSLContext.getInstance(sslProtocol)
        sc.init(null, trustAllCerts, SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        HttpsURLConnection.setDefaultHostnameVerifier(NoopHostnameVerifier())
        return Jsoup.connect(url)
    }

    private val Credentials.basicAuth: String?
        get() {
            val u = this.userName ?: return null
            val p = this.getPasswordAsString() ?: return null

            val basic = Base64.encode("$u:$p".toByteArray())
            return "Basic $basic"
        }

    companion object {
        fun getInstance(project: Project): HacHttpClient = project.service()
    }
}
