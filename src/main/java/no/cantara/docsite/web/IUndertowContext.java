package no.cantara.docsite.web;

import io.undertow.server.HttpServerExchange;

public interface IUndertowContext {

    HttpServerExchange getExchange();

}
