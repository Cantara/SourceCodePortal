package no.cantara.docsite.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class HttpRequests {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequests.class);

    static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    static String captureStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
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
        } catch (Exception e) {
            LOG.error("HttpRequest Error: {}", captureStackTrace(e));
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
            LOG.error("Error: {}", captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }
}
