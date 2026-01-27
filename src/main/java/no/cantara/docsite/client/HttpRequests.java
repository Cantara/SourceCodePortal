package no.cantara.docsite.client;

import no.cantara.docsite.commands.BaseResilientCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static no.cantara.docsite.util.CommonUtil.captureStackTrace;

public class HttpRequests {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequests.class);
    static final HttpClient HTTP_CLIENT;
    static final HttpClient HTTP_CLIENT_SHIELDS;

    static {
        /*
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sc;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
        */

        HTTP_CLIENT = HttpClient.newBuilder().build();

//        ForceHostnameVerificationSSLContext ctx = new ForceHostnameVerificationSSLContext("sni89405.cloudflaressl.com", 443);
//        HTTP_CLIENT_SHIELDS = HttpClient.newBuilder().sslContext(ctx).sslParameters(ctx.getParametersForSNI()).followRedirects(HttpClient.Redirect.ALWAYS).build();
        HTTP_CLIENT_SHIELDS = HttpClient.newBuilder().build();
    }

    public static HttpResponse<String> get(String uri, String... headers) {
        return get(uri, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), headers);
    }

    public static <R> HttpResponse<R> get(String uri, HttpResponse.BodyHandler<R> bodyHandler, String... headers) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(uri));
            if (headers != null && headers.length > 0) builder.headers(headers);
            HttpRequest request = builder.GET().build();
            return HTTP_CLIENT.send(request, bodyHandler);
        } catch (Throwable e) {
            if (e instanceof InterruptedException) {
                return BaseResilientCommand.getNullResponse(uri);
            }
            LOG.error("HttpRequest Error: {} => {}", uri, captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    public static <R> HttpResponse<R> getShieldsIO(String uri, HttpResponse.BodyHandler<R> bodyHandler, String... headers) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(uri));
            if (headers != null && headers.length > 0) builder.headers(headers);
            HttpRequest request = builder.GET().build();
            return HTTP_CLIENT_SHIELDS.send(request, bodyHandler);
        } catch (Throwable e) {
            if (e instanceof InterruptedException) {
                return BaseResilientCommand.getNullResponse(uri);
            }
            LOG.error("HttpRequest Error: {} => {}", uri, captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    public static HttpResponse<String> post(String uri, HttpRequest.BodyPublisher bodyPublisher, String... headers) {
        return post(uri, bodyPublisher, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), headers);
    }

    public static <R> HttpResponse<R> post(String uri, HttpRequest.BodyPublisher bodyPublisher, HttpResponse.BodyHandler<R> bodyHandler, String... headers) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(uri));
            if (headers != null && headers.length > 0) builder.headers(headers);
            HttpRequest request = builder.POST(bodyPublisher).build();
            return HTTP_CLIENT.send(request, bodyHandler);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                return BaseResilientCommand.getNullResponse(uri);
            }
            LOG.error("HttpRequest Error: {} => {}", uri, captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }
}
