package no.cantara.docsite.test.client;

import no.cantara.docsite.test.server.TestUriResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public final class TestClient {

    private static final Logger LOG = LoggerFactory.getLogger(TestClient.class);

    private final TestUriResolver server;
    private final HttpClient client;

    private TestClient(TestUriResolver server) {
        this.server = server;
        this.client = HttpClient.newBuilder().build();

        /*
        String username = "";
        String password = "";
        String basicAuth = new String(java.util.Base64.getEncoder().encode((username + ":" + password).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublisher.fromString("{\"some\":\"data\"");
        HttpRequest.newBuilder(URI.create(server.testURL("something")))
                .POST(bodyPublisher)
                .header("Content-Type", "application/json")
                .header("Authentication", "Basic " + basicAuth)
                .build();
        */
    }

    public static TestClient newClient(TestUriResolver server) {
        return new TestClient(server);
    }

    public TestUriResolver getTestUriResolver() {
        return server;
    }

    static String captureStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public ResponseHelper<String> options(String uri, String... headersKeyAndValue) {
        return options(uri, HttpResponse.BodyHandlers.ofString(), headersKeyAndValue);
    }

    public <R> ResponseHelper<R> options(String uri, HttpResponse.BodyHandler<R> bodyHandler, String... headersKeyAndValue) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(server.testURL(uri)))
                    .headers(headersKeyAndValue)
                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                    .build();
            return new ResponseHelper<>(client.send(request, bodyHandler));
        } catch (Exception e) {
            LOG.error("Error: {}", captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    public ResponseHelper<String> head(String uri) {
        return head(uri, HttpResponse.BodyHandlers.ofString());
    }

    public <R> ResponseHelper<R> head(String uri, HttpResponse.BodyHandler<R> bodyHandler) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(server.testURL(uri)))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();
            return new ResponseHelper<>(client.send(request, bodyHandler));
        } catch (Exception e) {
            LOG.error("Error: {}", captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    public ResponseHelper<String> put(String uri) {
        return put(uri, HttpRequest.BodyPublishers.noBody(), HttpResponse.BodyHandlers.ofString());
    }

    public ResponseHelper<String> put(String uri, String body) {
        return put(uri, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8), HttpResponse.BodyHandlers.ofString());
    }

    public <R> ResponseHelper<R> put(String uri, HttpRequest.BodyPublisher bodyPublisher, HttpResponse.BodyHandler<R> bodyHandler) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(server.testURL(uri)))
                    .PUT(bodyPublisher)
                    .build();
            return new ResponseHelper<>(client.send(request, bodyHandler));
        } catch (Exception e) {
            LOG.error("Error: {}", captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    public ResponseHelper<String> post(String uri) {
        return post(uri, HttpRequest.BodyPublishers.noBody(), HttpResponse.BodyHandlers.ofString());
    }

    public ResponseHelper<String> post(String uri, String body) {
        return post(uri, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8), HttpResponse.BodyHandlers.ofString());
    }

    public <R> ResponseHelper<R> post(String uri, HttpRequest.BodyPublisher bodyPublisher, HttpResponse.BodyHandler<R> bodyHandler) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(server.testURL(uri)))
                    .POST(bodyPublisher)
                    .build();
            return new ResponseHelper<>(client.send(request, bodyHandler));
        } catch (Exception e) {
            LOG.error("Error: {}", captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    public <R> ResponseHelper<R> postJson(String uri, HttpRequest.BodyPublisher bodyPublisher, HttpResponse.BodyHandler<R> bodyHandler) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(server.testURL(uri)))
                    .POST(bodyPublisher)
                    .header("Content-Type", "application/json")
                    .build();
            return new ResponseHelper<>(client.send(request, bodyHandler));
        } catch (Exception e) {
            LOG.error("Error: {}", captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    public <R> ResponseHelper<R> postForm(String uri, HttpRequest.BodyPublisher bodyPublisher, HttpResponse.BodyHandler<R> bodyHandler) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(server.testURL(uri)))
                    .POST(bodyPublisher)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            return new ResponseHelper<>(client.send(request, bodyHandler));
        } catch (Exception e) {
            LOG.error("Error: {}", captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    public ResponseHelper<String> get(String uri) {
        return get(uri, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    public <R> ResponseHelper<R> get(String uri, HttpResponse.BodyHandler<R> bodyHandler) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(server.testURL(uri)))
                    .GET()
                    .build();
            return new ResponseHelper<>(client.send(request, bodyHandler));
        } catch (Exception e) {
            LOG.error("Error: {}", captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    public ResponseHelper<String> delete(String uri) {
        return delete(uri, HttpResponse.BodyHandlers.ofString());
    }

    public <R> ResponseHelper<R> delete(String uri, HttpResponse.BodyHandler<R> bodyHandler) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(server.testURL(uri)))
                    .DELETE()
                    .build();
            return new ResponseHelper<>(client.send(request, bodyHandler));
        } catch (Exception e) {
            LOG.error("Error: {}", captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }
}
