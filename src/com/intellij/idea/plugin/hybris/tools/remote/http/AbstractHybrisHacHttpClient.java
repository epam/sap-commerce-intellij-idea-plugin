package com.intellij.idea.plugin.hybris.tools.remote.http;

import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService;
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent;
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
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
import org.apache.http.impl.client.HttpClientBuilder;
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
import static org.apache.http.HttpStatus.*;
import static org.apache.http.HttpVersion.HTTP_1_1;

public abstract class AbstractHybrisHacHttpClient {

    private static final Logger LOG = Logger.getInstance(AbstractHybrisHacHttpClient.class);
    private static final String COOKIE_ROUTE = "ROUTE";
    private static final String COOKIE_JSESSIONID = "JSESSIONID";
    public static final int DEFAULT_HAC_TIMEOUT = 6000;
    private static final X509TrustManager X_509_TRUST_MANAGER = new X509TrustManager() {

        @Override
        @Nullable
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(@NotNull final X509Certificate[] chain, final String authType) {
        }

        @Override
        public void checkServerTrusted(@NotNull final X509Certificate[] chain, final String authType) {
        }
    };

    private final Map<HybrisRemoteConnectionSettings, Map<String, String>> cookiesPerSettings = new WeakHashMap<>();

    public String login(@NotNull final Project project, @NotNull final HybrisRemoteConnectionSettings settings) {
        final var hostHacURL = getHostHacURL(project, settings);
        retrieveCookies(hostHacURL, project, settings);
        final var sessionId = Optional.ofNullable(cookiesPerSettings.get(settings))
            .map(it -> it.get(COOKIE_JSESSIONID))
            .orElse(null);
        if (sessionId == null) {
            return "Unable to obtain sessionId for " + hostHacURL;
        }
        final var csrfToken = getCsrfToken(hostHacURL, sessionId, project, settings);
        final var params = List.of(
            new BasicNameValuePair("j_username", settings.getHacLogin()),
            new BasicNameValuePair("j_password", settings.getHacPassword()),
            new BasicNameValuePair("_csrf", csrfToken)
        );
        final var loginURL = hostHacURL + "/j_spring_security_check";
        final HttpResponse response = post(project, loginURL, params, false, DEFAULT_HAC_TIMEOUT, settings);
        if (response.getStatusLine().getStatusCode() == SC_MOVED_TEMPORARILY) {
            final Header location = response.getFirstHeader("Location");
            if (location != null && location.getValue().contains("login_error")) {
                return "Wrong username/password. Set your credentials in [y] tool window.";
            }
        }
        final var newSessionId = CookieParser.getInstance().getSpecialCookie(response.getAllHeaders());
        if (newSessionId != null) {
            Optional.ofNullable(cookiesPerSettings.get(settings))
                .ifPresent(cookies -> cookies.put(COOKIE_JSESSIONID, newSessionId));
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
        final boolean canReLoginIfNeeded
    ) {
        final var settings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).getActiveHacRemoteConnectionSettings(project);
        return post(project, actionUrl, params, canReLoginIfNeeded, DEFAULT_HAC_TIMEOUT, settings);
    }

    @NotNull
    public final HttpResponse post(
        @NotNull final Project project,
        @NotNull final String actionUrl,
        @NotNull final List<BasicNameValuePair> params,
        final boolean canReLoginIfNeeded,
        final long timeout,
        final HybrisRemoteConnectionSettings settings
    ) {
        var cookies = cookiesPerSettings.get(settings);
        if (cookies == null || !cookies.containsKey(COOKIE_JSESSIONID)) {
            final String errorMessage = login(project, settings);
            if (StringUtils.isNotBlank(errorMessage)) {
                return createErrorResponse(errorMessage);
            }
        }
        cookies = cookiesPerSettings.get(settings);
        final var sessionId = cookies.get(COOKIE_JSESSIONID);
        final String csrfToken = getCsrfToken(getHostHacURL(project), sessionId, project, settings);
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

        boolean needsLogin = response.getStatusLine().getStatusCode() == SC_FORBIDDEN;
        if (response.getStatusLine().getStatusCode() == SC_MOVED_TEMPORARILY) {
            final Header location = response.getFirstHeader("Location");
            if (location != null && location.getValue().contains("login")) {
                needsLogin = true;
            }
        }

        if (needsLogin) {
            cookiesPerSettings.remove(settings);
            if (canReLoginIfNeeded) {
                return post(project, actionUrl, params, false, DEFAULT_HAC_TIMEOUT, settings);
            }
        }
        return response;
    }

    protected HttpResponse createErrorResponse(final String reasonPhrase) {
        return new BasicHttpResponse(new BasicStatusLine(HTTP_1_1, SC_SERVICE_UNAVAILABLE, reasonPhrase));
    }

    public String getHostHacURL(final Project project) {
        return CommonIdeaService.Companion.getInstance().getActiveHacUrl(project);
    }

    public String getSslProtocol(
        final Project project,
        final @Nullable HybrisRemoteConnectionSettings settings
    ) {
        return CommonIdeaService.Companion.getInstance().getActiveSslProtocol(project, settings);
    }

    public String getHostHacURL(final Project project, final HybrisRemoteConnectionSettings settings) {
        return CommonIdeaService.Companion.getInstance().getHostHacUrl(project, settings);
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
        final HttpClientBuilder builder = HttpClients.custom();
        builder.setConnectionManager(ccm);
        final RequestConfig config = RequestConfig.custom()
            .setSocketTimeout((int) timeout)
            .setConnectTimeout((int) timeout)
            .build();
        builder.setDefaultRequestConfig(config);
        return builder.build();
    }


    protected void retrieveCookies(
        final String hacURL,
        final @NotNull Project project,
        final @NotNull HybrisRemoteConnectionSettings settings
    ) {
        final var cookies = cookiesPerSettings.computeIfAbsent(settings, _settings -> new HashMap<>());
        cookies.clear();

        final var res = getResponseForUrl(hacURL, project, settings);

        if (res == null) return;

        cookies.putAll(res.cookies());
    }

    protected Response getResponseForUrl(
        final String hacURL,
        final @NotNull Project project,
        final @NotNull HybrisRemoteConnectionSettings settings
    ) {
        try {
            final String sslProtocol = getSslProtocol(project, settings);
            return connect(hacURL, sslProtocol).method(Method.GET).execute();
        } catch (ConnectException ce) {
            return null;
        } catch (NoSuchAlgorithmException | IOException | KeyManagementException e) {
            LOG.warn(e.getMessage(), e);
            return null;
        }
    }

    protected String getCsrfToken(
        final @NotNull String hacURL,
        final @NotNull String sessionId,
        final @NotNull Project project,
        final @Nullable HybrisRemoteConnectionSettings settings
    ) {
        try {
            final String sslProtocol = getSslProtocol(project, settings);

            final Document doc = connect(hacURL, sslProtocol).cookie("JSESSIONID", sessionId).get();
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
