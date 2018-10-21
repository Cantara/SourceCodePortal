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

        RepositoryConfig.Repo repositoryGroup = cacheStore.getGroupByGroupId(resourceContext.getLast().get().id);
        if (repositoryGroup == null) {
            return false;
        }

        templateVariables.put("repositoryConfig", repositoryGroup);
        templateVariables.put("repositoryGroups", cacheStore.getRepositoryGroupsByGroupId(repositoryGroup.groupId));

        if (ThymeleafViewEngineProcessor.processView(exchange, "/card/card", templateVariables)) {
            return true;
        }

        return false;
    }

}
