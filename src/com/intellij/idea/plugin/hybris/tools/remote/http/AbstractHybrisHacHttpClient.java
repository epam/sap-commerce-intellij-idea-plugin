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

package com.intellij.idea.plugin.hybris.tools.remote.http;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.ssl.SSLContexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.helper.ValidationException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.apache.http.HttpVersion.HTTP_1_1;

public abstract class AbstractHybrisHacHttpClient {

    private static final Logger LOG = Logger.getInstance(AbstractHybrisHacHttpClient.class);
    private static final Key<Replica> REPLICA_KEY = Key.create("hybris.http.replica");
    private final Project project;

    public static final int DEFAULT_HAC_TIMEOUT = 6000;

    private static final X509TrustManager X_509_TRUST_MANAGER = new X509TrustManager() {

        @Override
        @Nullable
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s) {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s) {
        }
    };

    private final Map<RemoteConnectionSettings, Map<String, String>> cookiesPerSettings = new WeakHashMap<>();

    public AbstractHybrisHacHttpClient(final Project project) {
        this.project = project;
    }

    @Nullable
    public Replica getReplica() {
        return project.getUserData(REPLICA_KEY);
    }

    public void setReplica(final Replica replica) {
        project.putUserData(REPLICA_KEY, replica);
        cookiesPerSettings.clear();
    }

    public String login(@NotNull final Project project, @NotNull final RemoteConnectionSettings settings) {
        final var hostHacURL = settings.getGeneratedURL();
        retrieveCookies(hostHacURL, project, settings);
        final var cookieName = getCookieName(settings);
        final var sessionId = Optional.ofNullable(cookiesPerSettings.get(settings))
            .map(it -> it.get(cookieName))
            .orElse(null);
        if (sessionId == null) {
            return "Unable to obtain sessionId for " + hostHacURL;
        }
        final var csrfToken = getCsrfToken(hostHacURL, settings);
        final var params = List.of(
            new BasicNameValuePair("j_username", settings.getUsername()),
            new BasicNameValuePair("j_password", settings.getPassword()),
            new BasicNameValuePair("_csrf", csrfToken)
        );
        final var loginURL = hostHacURL + "/j_spring_security_check";
        final HttpResponse response = post(project, loginURL, params, false, DEFAULT_HAC_TIMEOUT, settings);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
            final Header location = response.getFirstHeader("Location");
            if (location != null && location.getValue().contains("login_error")) {
                return "Wrong username/password. Set your credentials in [y] tool window.";
            }
        }
        final var newSessionId = CookieParser.getInstance().getSpecialCookie(response.getAllHeaders());
        if (newSessionId != null) {
            Optional.ofNullable(cookiesPerSettings.get(settings))
                .ifPresent(cookies -> cookies.put(cookieName, newSessionId));
            return StringUtils.EMPTY;
        }
        final int statusCode = response.getStatusLine().getStatusCode();
        final StringBuilder sb = new StringBuilder();
        sb.append("HTTP ");
        sb.append(statusCode);
        sb.append(' ');
        switch (statusCode) {
            case HTTP_OK -> sb.append("Unable to obtain sessionId from response");
            case HTTP_MOVED_TEMP -> sb.append(response.getFirstHeader("Location"));
            default -> sb.append(response.getStatusLine().getReasonPhrase());
        }
        return sb.toString();
    }

    @NotNull
    public final HttpResponse post(
        @NotNull final Project project,
        @NotNull final String actionUrl,
        @NotNull final List<BasicNameValuePair> params,
        final boolean canReLoginIfNeeded,
        final long timeout,
        final RemoteConnectionSettings settings
    ) {
        final String cookieName = getCookieName(settings);
        var cookies = cookiesPerSettings.get(settings);
        if (cookies == null || !cookies.containsKey(cookieName)) {
            final String errorMessage = login(project, settings);
            if (StringUtils.isNotBlank(errorMessage)) {
                return createErrorResponse(errorMessage);
            }
        }
        cookies = cookiesPerSettings.get(settings);
        final var sessionId = cookies.get(cookieName);
        final var csrfToken = getCsrfToken(settings.getGeneratedURL(), settings);
        if (csrfToken == null) {
            cookiesPerSettings.remove(settings);

            if (canReLoginIfNeeded) {
                return post(project, actionUrl, params, false, timeout, settings);
            }
            return createErrorResponse("Unable to obtain csrfToken for sessionId=" + sessionId);
        }
        final var client = createAllowAllClient(timeout);
        if (client == null) {
            return createErrorResponse("Unable to create HttpClient");
        }
        final var post = new HttpPost(actionUrl);
        final var cookie = cookies.entrySet().stream()
            .map(it -> it.getKey() + '=' + it.getValue())
            .collect(Collectors.joining("; "));
        post.setHeader("User-Agent", HttpHeaders.USER_AGENT);
        post.setHeader("X-CSRF-TOKEN", csrfToken);
        post.setHeader("Cookie", cookie);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        post.setHeader("Sec-Fetch-Dest", "empty");
        post.setHeader("Sec-Fetch-Mode", "cors");
        post.setHeader("Sec-Fetch-Site", "same-origin");

        final HttpResponse response;
        try {
            post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
            response = client.execute(post);
        } catch (IOException e) {
            LOG.warn(e.getMessage(), e);
            return createErrorResponse(e.getMessage());
        }

        final var statusCode = response.getStatusLine().getStatusCode();
        final var needsLogin = switch (statusCode) {
            case HttpStatus.SC_FORBIDDEN,
                 HttpStatus.SC_METHOD_NOT_ALLOWED -> true;
            case HttpStatus.SC_MOVED_TEMPORARILY -> {
                final var location = response.getFirstHeader("Location");
                yield location != null && location.getValue().contains("login");
            }
            default -> false;
        };

        if (needsLogin) {
            cookiesPerSettings.remove(settings);
            if (canReLoginIfNeeded) {
                return post(project, actionUrl, params, false, DEFAULT_HAC_TIMEOUT, settings);
            }
        }
        return response;
    }

    protected HttpResponse createErrorResponse(final String reasonPhrase) {
        return new BasicHttpResponse(new BasicStatusLine(HTTP_1_1, HttpStatus.SC_SERVICE_UNAVAILABLE, reasonPhrase));
    }

    protected CloseableHttpClient createAllowAllClient(final long timeout) {
        final SSLContext sslcontext;
        try {
            sslcontext = SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOG.warn(e.getMessage(), e);
            return null;
        }
        final SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslcontext, NoopHostnameVerifier.INSTANCE);

        final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", sslConnectionFactory)
            .build();

        final HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        final RequestConfig config = RequestConfig.custom()
            .setSocketTimeout((int) timeout)
            .setConnectTimeout((int) timeout)
            .build();
        return HttpClients.custom()
            .setConnectionManager(ccm)
            .setDefaultRequestConfig(config)
            .build();
    }


    protected void retrieveCookies(
        final String hacURL,
        final @NotNull Project project,
        final @NotNull RemoteConnectionSettings settings
    ) {
        final var cookies = cookiesPerSettings.computeIfAbsent(settings, _settings -> new HashMap<>());
        cookies.clear();

        final var res = getResponseForUrl(project, hacURL, settings);

        if (res == null) return;

        cookies.putAll(res.cookies());

        final var replica = getReplica();
        if (replica != null) {
            cookies.put(replica.getCookieName(), replica.getId());
        }
    }

    protected String getCookieName(@NotNull final RemoteConnectionSettings settings) {
        final var sessionCookieName = settings.getSessionCookieName();
        return StringUtils.isNotBlank(sessionCookieName) ? sessionCookieName : HybrisConstants.DEFAULT_SESSION_COOKIE_NAME;
    }

    @Nullable
    protected Response getResponseForUrl(
        final Project project,
        final String hacURL,
        final @NotNull RemoteConnectionSettings settings
    ) {
        try {
            final var sslProtocol = settings.getSslProtocol();
            final var connection = connect(hacURL, sslProtocol);
            final var replica = getReplica();

            if (replica != null) {
                connection.cookie(replica.getCookieName(), replica.getId());
            }

            return connection
                .method(Method.GET)
                .execute();
        } catch (final ConnectException ce) {
            return null;
        } catch (final NoSuchAlgorithmException | IOException | KeyManagementException | ValidationException e) {
            LOG.warn(e.getMessage(), e);
            return null;
        }
    }

    protected String getCsrfToken(
        final @NotNull String hacURL,
        final @NotNull RemoteConnectionSettings settings
    ) {
        try {
            final var sslProtocol = settings.getSslProtocol();

            final Document doc = connect(hacURL, sslProtocol)
                .cookies(cookiesPerSettings.get(settings))
                .get();
            final Elements csrfMetaElt = doc.select("meta[name=_csrf]");
            return csrfMetaElt.attr("content");
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            LOG.warn(e.getMessage(), e);
        }
        return null;
    }

    private Connection connect(@NotNull final String url, final String sslProtocol) throws NoSuchAlgorithmException, KeyManagementException {
        final TrustManager[] trustAllCerts = new TrustManager[]{X_509_TRUST_MANAGER};

        final SSLContext sc = SSLContext.getInstance(sslProtocol);
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new NoopHostnameVerifier());
        return Jsoup.connect(url);
    }
}
