package no.cantara.docsite.controller;

import io.undertow.io.Receiver;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.cantara.docsite.json.JsonbFactory;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;

public class EchoController implements HttpHandler {

    private static final String ACCESS_TOKEN = "AccessTokenTest";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        {
            JsonObjectBuilder childBuilder = Json.createObjectBuilder();
            exchange.getRequestHeaders().getHeaderNames().forEach(h -> {
                exchange.getRequestHeaders().eachValue(h).forEach(v -> {
                    childBuilder.add(h.toString(), v);
                });
            });
            builder.add("request-headers", childBuilder);
        }

        {
            JsonObjectBuilder childBuilder = Json.createObjectBuilder();
            childBuilder.add("uri", exchange.getRequestURI());
            childBuilder.add("method", exchange.getRequestMethod().toString());
            childBuilder.add("statusCode", String.valueOf(exchange.getStatusCode()));
            childBuilder.add("isSecure", Boolean.valueOf(exchange.isSecure()).toString());
            childBuilder.add("sourceAddress", exchange.getSourceAddress().toString());
            childBuilder.add("destinationAddress", exchange.getDestinationAddress().toString());
            builder.add("request-info", childBuilder);

        }

        {
            JsonObjectBuilder childBuilder = Json.createObjectBuilder();
            exchange.getRequestCookies().forEach((k, v) -> {
                childBuilder.add(k, v.getValue());
            });
            builder.add("cookies", childBuilder);
        }

        {
            JsonObjectBuilder childBuilder = Json.createObjectBuilder();
            exchange.getPathParameters().entrySet().forEach((e) -> {
                childBuilder.add(e.getKey(), e.getValue().element());
            });
            builder.add("path-parameters", childBuilder);
        }

        {
            builder.add("queryString", exchange.getQueryString());
            JsonObjectBuilder childBuilder = Json.createObjectBuilder();
            exchange.getQueryParameters().entrySet().forEach((e) -> {
                childBuilder.add(e.getKey(), e.getValue().element());
            });
            builder.add("query-parameters", childBuilder);
        }

        {
            JsonObjectBuilder childBuilder = Json.createObjectBuilder();
            builder.add("contentLength", String.valueOf(exchange.getRequestContentLength()));
            exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
                @Override
                public void handle(HttpServerExchange httpServerExchange, byte[] bytes) {
                    childBuilder.add("payload", new String(bytes));
                }
            });
            builder.add("request-body", childBuilder);
        }

        {
            JsonObjectBuilder childBuilder = Json.createObjectBuilder();
            exchange.getResponseHeaders().getHeaderNames().forEach(h -> {
                exchange.getResponseHeaders().eachValue(h).forEach(v -> {
                    childBuilder.add(h.toString(), v);
                });
            });
            builder.add("response-headers", childBuilder);
        }

        {
            JsonObjectBuilder childBuilder = Json.createObjectBuilder();
            exchange.getResponseCookies().forEach((k, v) -> {
                childBuilder.add(k, v.getValue());
            });
            builder.add("response-cookies", childBuilder);
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod().toString()))
            exchange.setStatusCode(HTTP_OK);
        else if ("POST".equalsIgnoreCase(exchange.getRequestMethod().toString()))
            exchange.setStatusCode(HTTP_NO_CONTENT);
        else
            throw new UnsupportedOperationException("Method " + exchange.getRequestMethod() + " not supported!");

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(JsonbFactory.prettyPrint(builder.build()));
    }
}
