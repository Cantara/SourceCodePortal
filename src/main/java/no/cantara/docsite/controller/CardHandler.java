package no.cantara.docsite.controller;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepositoryConfig;
import no.cantara.docsite.web.ResourceContext;
import no.cantara.docsite.web.ThymeleafViewEngineProcessor;
import no.cantara.docsite.web.WebContext;
import no.cantara.docsite.web.WebHandler;
import no.ssb.config.DynamicConfiguration;

import java.util.HashMap;
import java.util.Map;

public class CardHandler implements WebHandler {

    @Override
    public boolean handleRequest(DynamicConfiguration configuration, CacheStore cacheStore, ResourceContext resourceContext, WebContext webContext, HttpServerExchange exchange) {
        Map<String, Object> templateVariables = new HashMap<>();

        RepositoryConfig.Repo repositoryConfig = cacheStore.getGroupByGroupId(resourceContext.getLast().get().id);
        if (repositoryConfig == null) {
            return false;
        }

        templateVariables.put("repositoryConfig", repositoryConfig);
        templateVariables.put("repositoryGroup", cacheStore.getRepositoryGroupsByGroupId(repositoryConfig.groupId));

        if (ThymeleafViewEngineProcessor.processView(exchange, webContext.asTemplateResource(), templateVariables)) {
            return true;
        }

        return false;
    }

}
