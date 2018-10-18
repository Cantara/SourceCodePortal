package no.cantara.docsite.web;

import io.undertow.server.HttpServerExchange;
import org.thymeleaf.context.AbstractContext;

public class UndertowContext extends AbstractContext implements IUndertowContext {

    private final HttpServerExchange exchange;

    public UndertowContext(HttpServerExchange exchange) {
        super();
        this.exchange = exchange;
    }

    @Override
    public HttpServerExchange getExchange() {
        System.out.println("----------------------------> " + exchange.getRequestPath());
        return exchange;
    }

}
