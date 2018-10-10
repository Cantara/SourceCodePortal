package no.cantara.docsite.web;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.cantara.docsite.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

public class ThymeleafViewEngineProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ThymeleafViewEngineProcessor.class);

    static URL resolveTemplate(String resourceName) throws MalformedURLException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        LOG.trace("ResourceName: {}", resourceName);
        URL file = classLoader.getResource(resourceName);
        if (file == null) {
            throw new RuntimeException("Unable to load resource: " + resourceName);
        }
        return file;
    }

    static boolean supports(String view) {
        return view.endsWith(".html");
    }

    static String resolveView(HttpServerExchange exchange) {
        return String.format("META-INF/views%s.html", exchange.getRequestURI());
    }

    public static boolean processView(HttpServerExchange exchange, Map<String, Object> templateVariables) throws RuntimeException {
        try {
            UndertowContext ctx = new UndertowContext(exchange);
            ctx.setLocale(Locale.ENGLISH);
            ctx.setVariables(templateVariables);

            LOG.trace("Models Size: {}", ctx.getVariableNames().size());
            ctx.getVariableNames().forEach(k -> {
                LOG.trace("Model: {} = {}", k, ctx.getVariable(k));
            });
            String viewId = resolveView(exchange);

            LOG.info("viewId: {}", viewId);

            LOG.trace("Process MVC Template: {} => {} ", viewId, (viewId != null && !"".equals(viewId) ? resolveTemplate(viewId).getFile() : null));

            try (OutputStream out = CommonUtil.newOutputStream()) {
                InputStream file = ClassLoader.getSystemResourceAsStream(viewId);
                CommonUtil.writeInputToOutputStream(file, out);
                LOG.trace("Template:\n{}", out);
            }

            StringWriter writer = new StringWriter();
            DefaultTemplateEngine.INSTANCE.process(viewId, ctx, writer);

            exchange.setStatusCode(200);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
            exchange.getResponseSender().send(writer.toString());

            return true;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
