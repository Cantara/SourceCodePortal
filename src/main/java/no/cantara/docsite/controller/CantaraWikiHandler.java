package no.cantara.docsite.controller;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheCantaraWikiKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.web.ResourceContext;
import no.cantara.docsite.web.ThymeleafViewEngineProcessor;
import no.cantara.docsite.web.WebContext;
import no.cantara.docsite.web.WebHandler;
import no.ssb.config.DynamicConfiguration;

import javax.cache.Cache;
import java.util.HashMap;
import java.util.Map;

public class CantaraWikiHandler implements WebHandler {

    @Override
    public boolean handleRequest(DynamicConfiguration configuration, CacheStore cacheStore, ResourceContext resourceContext, WebContext webContext, HttpServerExchange exchange) {
        Map<String, Object> templateVariables = new HashMap<>();

        String pageName = resourceContext.getLast().get().id;
        if (pageName == null || "".equals(pageName)) {
            return false;
        }

        CacheCantaraWikiKey cacheCantaraWikiKey = null;
        for(Cache.Entry<CacheCantaraWikiKey, String> entry : cacheStore.getCantaraWiki()) {
            if (pageName.equals(entry.getKey().pageName)) {
                cacheCantaraWikiKey = entry.getKey();
                break;
            }
        }

        if (cacheCantaraWikiKey == null) {
            return false;
        }

        String wikiContent = cacheStore.getCantaraWiki().get(cacheCantaraWikiKey);

        templateVariables.put("wikiHtml", wikiContent);

        if (ThymeleafViewEngineProcessor.processView(exchange, cacheStore, webContext.asTemplateResource(), templateVariables)) {
            return true;
        }

        return false;
    }
}
