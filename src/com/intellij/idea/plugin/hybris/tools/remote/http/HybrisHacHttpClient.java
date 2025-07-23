/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.ReplicaContext;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
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
import org.jsoup.Jsoup;
import org.jsoup.helper.ValidationException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.Serial;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.apache.http.HttpVersion.HTTP_1_1;

@Service(Service.Level.PROJECT)
public final class HybrisHacHttpClient extends UserDataHolderBase {

    private static final Logger LOG = Logger.getInstance(HybrisHacHttpClient.class);
    public static final int DEFAULT_HAC_TIMEOUT = 6000;

    @Serial
    private static final long serialVersionUID = -4915832410081381025L;

    private static final X509TrustManager X_509_TRUST_MANAGER = new X509TrustManager() {

        @Override
        @Nullable
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
        }
    };

    private final Map<String, Map<String, String>> cookiesPerSettings = new ConcurrentHashMap<>();

    public static HybrisHacHttpClient getInstance(final Project project) {
        return project.getService(HybrisHacHttpClient.class);
    }

    @NotNull
    public String testConnection(@NotNull final RemoteConnectionSettings settings) {
        return login(settings, null, getCookiesKey(settings, null));
    }

    @NotNull
    public HttpResponse post(
        @NotNull final String actionUrl,
        @NotNull final List<BasicNameValuePair> params,
        final boolean canReLoginIfNeeded,
        final int timeout,
        final RemoteConnectionSettings settings,
        @Nullable final ReplicaContext replicaContext
    ) {
        final var cookiesKey = getCookiesKey(settings, replicaContext);
        final String cookieName = getCookieName(settings);
        var cookies = cookiesPerSettings.get(cookiesKey);
        if (cookies == null || !cookies.containsKey(cookieName)) {
            final String errorMessage = login(settings, replicaContext, cookiesKey);
            if (StringUtils.isNotBlank(errorMessage)) {
                return createErrorResponse(errorMessage);
            }
        }
        cookies = cookiesPerSettings.get(cookiesKey);
        if (cookies == null) return createErrorResponse("Unable to authenticate request.");

        final var sessionId = cookies.get(cookieName);
        final var csrfToken = getCsrfToken(settings.getGeneratedURL(), settings, cookiesKey);
        if (csrfToken == null) {
            cookiesPerSettings.remove(cookiesKey);

            if (canReLoginIfNeeded) {
                return post(actionUrl, params, false, timeout, settings, replicaContext);
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
            cookiesPerSettings.remove(cookiesKey);
            if (canReLoginIfNeeded) {
                return post(actionUrl, params, false, HybrisHacHttpClient.DEFAULT_HAC_TIMEOUT, settings, replicaContext);
            }
        }
        return response;
    }

    private String login(
        @NotNull final RemoteConnectionSettings settings,
        @Nullable final ReplicaContext replicaContext,
        final String cookiesKey
    ) {
        final var hostHacURL = settings.getGeneratedURL();

        retrieveCookies(hostHacURL, settings, replicaContext, cookiesKey);

        final var cookieName = getCookieName(settings);
        final var sessionId = Optional.ofNullable(cookiesPerSettings.get(cookiesKey))
            .map(it -> it.get(cookieName))
            .orElse(null);
        if (sessionId == null) {
            return "Unable to obtain sessionId for " + hostHacURL;
        }
        final var csrfToken = getCsrfToken(hostHacURL, settings, cookiesKey);
        final var params = List.of(
            new BasicNameValuePair("j_username", settings.getUsername()),
            new BasicNameValuePair("j_password", settings.getPassword()),
            new BasicNameValuePair("_csrf", csrfToken)
        );
        final var loginURL = hostHacURL + "/j_spring_security_check";
        final HttpResponse response = post(loginURL, params, false, HybrisHacHttpClient.DEFAULT_HAC_TIMEOUT, settings, replicaContext);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
            final Header location = response.getFirstHeader("Location");
            if (location != null && location.getValue().contains("login_error")) {
                return "Wrong username/password. Set your credentials in [y] tool window.";
            }
        }
        final var newSessionId = CookieParser.getInstance().getSpecialCookie(response.getAllHeaders());
        if (newSessionId != null) {
            Optional.ofNullable(cookiesPerSettings.get(cookiesKey))
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

    private HttpResponse createErrorResponse(final String reasonPhrase) {
        return new BasicHttpResponse(new BasicStatusLine(HTTP_1_1, HttpStatus.SC_SERVICE_UNAVAILABLE, reasonPhrase));
    }

    private CloseableHttpClient createAllowAllClient(final int timeout) {
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
            .setSocketTimeout(timeout)
            .setConnectTimeout(timeout)
            .build();
        return HttpClients.custom()
            .setConnectionManager(ccm)
            .setDefaultRequestConfig(config)
            .build();
    }


    private void retrieveCookies(
        final String hacURL,
        final @NotNull RemoteConnectionSettings settings,
        final @Nullable ReplicaContext replicaContext,
        final String cookiesKey
    ) {
        final var cookies = cookiesPerSettings.computeIfAbsent(cookiesKey, _settings -> new HashMap<>());
        cookies.clear();

        final var res = getResponseForUrl(hacURL, settings, replicaContext);

        if (res == null) return;

        cookies.putAll(res.cookies());

        if (replicaContext != null) {
            cookies.put(replicaContext.getCookieName(), replicaContext.getReplicaCookie());
        }
    }

    private String getCookieName(@NotNull final RemoteConnectionSettings settings) {
        final var sessionCookieName = settings.getSessionCookieName();
        return StringUtils.isNotBlank(sessionCookieName) ? sessionCookieName : HybrisConstants.DEFAULT_SESSION_COOKIE_NAME;
    }

    @Nullable
    private Connection.Response getResponseForUrl(
        final String hacURL,
        final @NotNull RemoteConnectionSettings settings,
        final @Nullable ReplicaContext replicaContext
    ) {
        try {
            final var sslProtocol = settings.getSslProtocol();
            final var connection = connect(hacURL, sslProtocol);

            if (replicaContext != null) {
                connection.cookie(replicaContext.getCookieName(), replicaContext.getReplicaCookie());
            }

            return connection
                .method(Connection.Method.GET)
                .execute();
        } catch (final ConnectException ce) {
            return null;
        } catch (final NoSuchAlgorithmException | IOException | KeyManagementException | ValidationException e) {
            LOG.warn(e.getMessage(), e);
            return null;
        }
    }

    private String getCsrfToken(
        final @NotNull String hacURL,
        final @NotNull RemoteConnectionSettings settings,
        final String cookiesKey
    ) {
        try {
            final var sslProtocol = settings.getSslProtocol();

            final Document doc = connect(hacURL, sslProtocol)
                .cookies(cookiesPerSettings.get(cookiesKey))
                .get();
            final Elements csrfMetaElt = doc.select("meta[name=_csrf]");
            return csrfMetaElt.attr("content");
        } catch (final IOException | NoSuchAlgorithmException | KeyManagementException e) {
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

    private String getCookiesKey(final RemoteConnectionSettings settings, @Nullable final ReplicaContext context) {
        return "%s_%s".formatted(
            settings.getUuid(),
            context == null ? "auto" : context.getReplicaId()
        );
    }
}
