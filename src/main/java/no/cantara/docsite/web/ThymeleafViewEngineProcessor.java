package no.cantara.docsite.web;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.cantara.docsite.cache.CacheHelper;
import no.cantara.docsite.cache.CacheStore;

import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

public class ThymeleafViewEngineProcessor {

    static boolean templateExists(String resourceName) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        return classLoader.getResource(DefaultTemplateEngine.CLASS_RESOURCE_PATH + resourceName) != null;
    }

    public static boolean processView(HttpServerExchange exchange, CacheStore cacheStore, String viewId, Map<String, Object> templateVariables) throws RuntimeException {
        UndertowContext ctx = new UndertowContext(exchange);
        ctx.setLocale(Locale.ENGLISH);

        {
            templateVariables.put("connectedRepos", CacheHelper.cacheSize(cacheStore.getRepositories()));
        }

        ctx.setVariables(templateVariables);

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
