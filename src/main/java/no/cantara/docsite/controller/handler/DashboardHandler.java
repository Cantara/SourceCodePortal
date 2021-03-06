package no.cantara.docsite.controller.handler;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.scm.ScmCommitRevision;
import no.cantara.docsite.domain.scm.ScmCommitRevisionService;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.cantara.docsite.domain.scm.ScmRepositoryGroup;
import no.cantara.docsite.domain.scm.ScmRepositoryService;
import no.cantara.docsite.web.ResourceContext;
import no.cantara.docsite.web.ThymeleafViewEngineProcessor;
import no.cantara.docsite.web.WebContext;
import no.cantara.docsite.web.WebHandler;
import no.ssb.config.DynamicConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardHandler implements WebHandler {

    @Override
    public boolean handleRequest(DynamicConfiguration configuration, CacheStore cacheStore, ResourceContext resourceContext, WebContext webContext, HttpServerExchange exchange) {
        Map<String, Object> templateVariables = new HashMap<>();

        ScmCommitRevisionService commitRevisionService = new ScmCommitRevisionService(cacheStore);
        List<ScmCommitRevision> lastCommitRevisions = commitRevisionService.entrySet().values().stream().limit(5).collect(Collectors.toList());;
        templateVariables.put("lastCommitRevisions", lastCommitRevisions);

        ScmRepositoryService scmRepositoryService = new ScmRepositoryService(cacheStore);
        Map<CacheRepositoryKey, ScmRepositoryGroup<ScmRepository>> repositoryGroups = scmRepositoryService.defaultRepositoryGroups();
        templateVariables.put("repositoryGroups", repositoryGroups);

        if (ThymeleafViewEngineProcessor.processView(exchange, cacheStore, webContext.asTemplateResource(), templateVariables)) {
            return true;
        }

        return false;
    }
}
