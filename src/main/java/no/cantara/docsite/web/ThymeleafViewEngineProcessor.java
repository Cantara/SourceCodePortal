package no.cantara.docsite.web;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

public class ThymeleafViewEngineProcessor {

    static boolean templateExists(String resourceName) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        return classLoader.getResource(DefaultTemplateEngine.CLASS_RESOURCE_PATH + resourceName) != null;
    }

    static String resolveView(HttpServerExchange exchange, String subContext) {
        return (subContext == null || "".equals(subContext) ? String.format("%s.html", exchange.getRequestURI()) : String.format("/%s%s.html", subContext, exchange.getRequestURI()));
    }

    static String resolveView(String pageURI) {
        return String.format("%s.html", pageURI);
    }

    public static boolean processView(HttpServerExchange exchange, String pageURI, Map<String, Object> templateVariables) throws RuntimeException {
        UndertowContext ctx = new UndertowContext(exchange);
        ctx.setLocale(Locale.ENGLISH);
        ctx.setVariables(templateVariables);
        String viewId = resolveView(pageURI);

        if (!templateExists(viewId)) {
            return false;
        }

        StringWriter writer = new StringWriter();
        DefaultTemplateEngine.INSTANCE.process(viewId, ctx, writer);

        exchange.setStatusCode(200);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
        exchange.getResponseSender().send(writer.toString());

        return true;
    }

}
