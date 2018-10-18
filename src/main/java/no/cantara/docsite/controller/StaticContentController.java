package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class StaticContentController implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(StaticContentController.class);
    private DynamicConfiguration configuration;
    private String resourcePath;
    private String contentType;

    public StaticContentController(DynamicConfiguration configuration, String resourcePath, String contentType) {
        this.configuration = configuration;
        this.resourcePath = resourcePath;
        this.contentType = contentType;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        // TODO configure if static resources should be cached
        URL url = ClassLoader.getSystemResource(resourcePath + exchange.getRequestPath());
        if (url != null) {
            exchange.setStatusCode(200);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType);
            try (InputStream in = url.openStream()) {
                exchange.getResponseSender().send(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
            return;
        }

        exchange.setStatusCode(404);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Not found: " + exchange.getRequestPath());
    }
}
