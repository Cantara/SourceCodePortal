package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

public class ImageResourceController implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ImageResourceController.class);
    private DynamicConfiguration configuration;
    private String resourcePath;

    public ImageResourceController(DynamicConfiguration configuration, String resourcePath) {
        this.configuration = configuration;
        this.resourcePath = resourcePath;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        // TODO configure if static resources should be cached
        String resourceURL = ("/favicon.ico".equalsIgnoreCase(exchange.getRequestPath()) ? resourcePath + "/img" + exchange.getRequestPath() : resourcePath + exchange.getRequestPath());
        URL url = ClassLoader.getSystemResource(resourceURL);
        if (url != null) {
            exchange.setStatusCode(200);
            String contentType = null;
            if (exchange.getRequestPath().toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (exchange.getRequestPath().toLowerCase().endsWith(".jpg") || exchange.getRequestPath().toLowerCase().endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (exchange.getRequestPath().toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            } else if (exchange.getRequestPath().toLowerCase().endsWith(".svg")) {
                contentType = "image/svg+xml";
            } else if (exchange.getRequestPath().toLowerCase().endsWith(".ico")) {
                contentType = "image/x-icon";
            }
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType);
            try (InputStream in = url.openStream()) {
                byte[] bytes = in.readAllBytes();
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                exchange.setResponseContentLength(bytes.length);
                exchange.getResponseSender().send(byteBuffer);
            }
            return;
        }

        exchange.setStatusCode(404);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Not found: " + exchange.getRequestPath());
    }
}
