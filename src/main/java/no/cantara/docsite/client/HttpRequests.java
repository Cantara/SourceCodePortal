package no.cantara.docsite.client;

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
        HTTP_CLIENT = HttpClient.newBuilder().build();

        ForceHostnameVerificationSSLContext ctx = new ForceHostnameVerificationSSLContext("sni89405.cloudflaressl.com", 443);
        HTTP_CLIENT_SHIELDS = HttpClient.newBuilder().sslContext(ctx).sslParameters(ctx.getParametersForSNI()).followRedirects(HttpClient.Redirect.ALWAYS).build();
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
            LOG.error("HttpRequest Error: {} => {}", uri, captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }
}
