package no.cantara.docsite.controller;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.Repository;
import no.cantara.docsite.domain.config.RepositoryConfig;
import no.cantara.docsite.web.ResourceContext;
import no.cantara.docsite.web.ThymeleafViewEngineProcessor;
import no.cantara.docsite.web.WebContext;
import no.cantara.docsite.web.WebHandler;
import no.ssb.config.DynamicConfiguration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RootHandler implements WebHandler {

    @Override
    public boolean handleRequest(DynamicConfiguration configuration, CacheStore cacheStore, ResourceContext resourceContext, WebContext webContext, HttpServerExchange exchange) {
        Map<String, Object> templateVariables = new HashMap<>();

        templateVariables.put("groups", cacheStore.getGroups());

        Map<RepositoryConfig.Repo, List<Repository>> repositoryGroups = new LinkedHashMap<>();
        for (RepositoryConfig.Repo group : cacheStore.getGroups()) {
            repositoryGroups.put(group, cacheStore.getRepositoryGroupsByGroupId(group.groupId));
        }
        templateVariables.put("repositoryGroups", repositoryGroups);

        if (ThymeleafViewEngineProcessor.processView(exchange, String.format("%s", webContext.uri), templateVariables)) {
            return true;
        }

        return false;
    }
}
