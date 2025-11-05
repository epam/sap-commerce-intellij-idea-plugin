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
import sap.commerce.toolset.hac.exec.settings.state.AuthenticationMode
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import kotlin.io.encoding.Base64

@Service(Service.Level.PROJECT)
class HacHttpClient(private val project: Project) {

    private val cookiesPerSettings = ConcurrentHashMap<String, MutableMap<String, String>>()

    suspend fun testConnection(
        settings: HacConnectionSettingsState,
        username: String,
        password: String,
    ): HacHttpAuthenticationResult {
        val cookiesKey = HttpCookiesCache.getInstance(project).getKey(settings)
        return authenticate(settings, cookiesKey, username, password)
            .also { cookiesPerSettings.remove(cookiesKey) }
    }

    suspend fun post(
        actionUrl: String,
        params: Collection<BasicNameValuePair>,
        canReLoginIfNeeded: Boolean,
        timeout: Int,
        settings: HacConnectionSettingsState,
        replicaContext: ReplicaContext?
    ): HttpResponse {
        val cookiesKey = HttpCookiesCache.getInstance(project).getKey(settings, replicaContext)
        val sessionCookieName = getSessionCookieName(settings)
        var cookies = cookiesPerSettings[cookiesKey]
        var prefilledCsrfToken: String? = null
        var authorization: Credentials? = if (!settings.proxyAuthentication || settings.authenticationMode == AuthenticationMode.MANUAL) null
        // TODO : Use credentials from settings
        else null

        if (cookies == null || !cookies.containsKey(sessionCookieName)) {
            if (settings.authenticationMode == AuthenticationMode.MANUAL) {
                val authenticationContext = HacManualAuthenticator.getService(project)
                    .authenticate(settings)
                    ?.takeIf { it.isValid(sessionCookieName) }
                    ?: return createErrorResponse("Unable to find cookie $sessionCookieName")

                cookiesPerSettings[cookiesKey] = authenticationContext.cookies.toMutableMap()
                prefilledCsrfToken = authenticationContext.csrfToken
                authorization = authenticationContext.authorization
            } else {
                val credentials = HacExecConnectionService.getInstance(project).getCredentials(settings)
                val username = credentials.userName ?: ""
                val password = credentials.getPasswordAsString() ?: ""
                val authResult = authenticate(settings, cookiesKey, username, password, replicaContext)

                if (authResult is HacHttpAuthenticationResult.Error) {
                    return createErrorResponse(authResult.message)
                }
            }
        }

        cookies = cookiesPerSettings[cookiesKey]
            ?: return createErrorResponse("Unable to authenticate request.")

        val sessionId = cookies[sessionCookieName]
        val generatedURL = settings.generatedURL
        val csrfToken = prefilledCsrfToken
            ?: getCsrfToken(generatedURL, settings, cookies)

        if (csrfToken == null) {
            cookiesPerSettings.remove(cookiesKey)

            if (canReLoginIfNeeded) {
                return post(actionUrl, params, false, timeout, settings, replicaContext)
            }
            return createErrorResponse("Unable to obtain csrfToken for sessionId=$sessionId")
        }

        val client = createAllowAllClient(timeout)
            ?: return createErrorResponse("Unable to create HttpClient")

        val post = HttpPost(actionUrl).apply {
            authorization?.let { setHeader("Authorization", it.basicAuth) }

            setHeader("User-Agent", HttpHeaders.USER_AGENT)
            setHeader("X-CSRF-TOKEN", csrfToken)
            setHeader("Cookie", cookies.entries.joinToString("; ") { it.key + "=" + it.value })
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
            cookiesPerSettings.remove(cookiesKey)
            if (canReLoginIfNeeded) {
                return post(actionUrl, params, false, timeout, settings, replicaContext)
            }
        }
        return response
    }

    private suspend fun authenticate(
        settings: HacConnectionSettingsState,
        cookiesKey: String,
        username: String,
        password: String,
        replicaContext: ReplicaContext? = null
    ): HacHttpAuthenticationResult {
        val hostHacURL = settings.generatedURL

        retrieveCookies(hostHacURL, settings, replicaContext, cookiesKey)

        val sessionCookieName = getSessionCookieName(settings)
        val cookies = cookiesPerSettings.get(cookiesKey)
        cookies?.get(sessionCookieName)
            ?: return HacHttpAuthenticationResult.Error(hostHacURL, "Unable to obtain sessionId for $hostHacURL")

        val csrfToken = getCsrfToken(hostHacURL, settings, cookies)
            ?: return HacHttpAuthenticationResult.Error(hostHacURL, "Unable to obtain csrfToken for $hostHacURL")

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
                return HacHttpAuthenticationResult.Error(hostHacURL, "Wrong username/password. Set your credentials in [y] tool window.")
            }
        }

        return CookieParser.getInstance().getSpecialCookie(response.allHeaders)
            ?.let { newSessionId ->
                cookiesPerSettings[cookiesKey]?.let { it[sessionCookieName] = newSessionId }
                HacHttpAuthenticationResult.Success(hostHacURL)
            }
            ?: HacHttpAuthenticationResult.Error(hostHacURL, buildString {
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
        cookiesKey: String
    ) {
        val cookies = cookiesPerSettings.computeIfAbsent(cookiesKey) { mutableMapOf() }
        cookies.clear()

        val res = getResponseForUrl(hacURL, settings, replicaContext)
            ?: return

        cookies.putAll(res.cookies())

        if (replicaContext != null) {
            cookies[replicaContext.cookieName] = replicaContext.replicaCookie
        }
    }

    private fun getSessionCookieName(settings: HacConnectionSettingsState) = settings.sessionCookieName
        .takeIf { it.isNotBlank() }
        ?: ExecConstants.DEFAULT_SESSION_COOKIE_NAME

    private fun getResponseForUrl(
        hacURL: String,
        settings: HacConnectionSettingsState,
        replicaContext: ReplicaContext?
    ): Connection.Response? {
        try {
            val connection = connect(hacURL, settings.sslProtocol)

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
        cookies: Map<String, String>
    ): String? = try {
        connect(hacURL, settings.sslProtocol)
            .cookies(cookies)
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
