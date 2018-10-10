package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class CORSController implements HttpHandler {

    private final String corsAllowOrigin;
    private final String corsAllowHeaders;
    private final boolean corsAllowOriginTest;
    private final int undertowPort;

    private HttpServerExchange exchange;
    private boolean validOrigin;

    CORSController(String corsAllowOrigin, String corsAllowHeaders, boolean corsAllowOriginTest, int undertowPort) {
        this.corsAllowOrigin = corsAllowOrigin;
        this.corsAllowHeaders = corsAllowHeaders;
        this.corsAllowOriginTest = corsAllowOriginTest;
        this.undertowPort = undertowPort;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        this.exchange = exchange;
        this.validOrigin = validateAllowOrigin(exchange);
    }

    private boolean validateAllowOrigin(HttpServerExchange exchange) {
        if ("*".equals(corsAllowOrigin)) {
            return true;

        } else {
            String[] allowedDomains = corsAllowOrigin.split(",");
            String requestDomain = String.format("%s://%s:%s", exchange.getRequestScheme(), exchange.getHostName(), exchange.getHostPort());
            boolean acceptDomain = false;
            for (int n = 0; n < allowedDomains.length; n++) {
                try {
                    URL url = new URL(allowedDomains[n]);
                    String corsAllowDomain = String.format("%s://%s:%s", url.getProtocol(), url.getHost(), (corsAllowOriginTest ? undertowPort : url.getPort()));
                    if (corsAllowDomain.equals(requestDomain)) {
                        acceptDomain = true;
                        break;
                    }
                } catch (MalformedURLException me) {
                    String msg = String.format("The configuration 'http.cors.allow.origin' contains a malformed URL: %s", allowedDomains[n]);
                    throw new RuntimeException(msg, me);
                }
            }
            return acceptDomain;
        }
    }

    private void applyCORSHeaders() {
        exchange.getResponseHeaders().put(HttpString.tryFromString("Access-Control-Allow-Origin"), corsAllowOrigin);
        exchange.getResponseHeaders().put(HttpString.tryFromString("Access-Control-Allow-Methods"), "GET, PUT, DELETE, OPTIONS, HEAD");
        exchange.getResponseHeaders().put(HttpString.tryFromString("Access-Control-Allow-Headers"), corsAllowHeaders);
    }

    boolean isBadRequest() {
        if (!validOrigin) {
            exchange.setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
            return true;
        }
        return false;
    }

    boolean isOptionsRequest() {
        return exchange.getRequestMethod().equalToString("options");
    }

    boolean doOptions() {
        if (validOrigin) {
            exchange.setStatusCode(HttpURLConnection.HTTP_ACCEPTED);
            applyCORSHeaders();
            return true;
        }
        return false;
    }

    void handleValidRequest() {
        if (validOrigin) {
            applyCORSHeaders();
        } else {
            throw new RuntimeException("Something is wrong with the CORSController!");
        }
    }

}
