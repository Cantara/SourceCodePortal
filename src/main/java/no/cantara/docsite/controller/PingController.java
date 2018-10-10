package no.cantara.docsite.controller;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.net.HttpURLConnection;

class PingController implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        exchange.setStatusCode(HttpURLConnection.HTTP_OK);
    }
}
