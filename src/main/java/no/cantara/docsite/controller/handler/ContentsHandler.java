package no.cantara.docsite.controller.handler;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepoConfig;
import no.cantara.docsite.domain.scm.ScmRepositoryContents;
import no.cantara.docsite.domain.scm.ScmRepositoryContentsService;
import no.cantara.docsite.web.ResourceContext;
import no.cantara.docsite.web.ThymeleafViewEngineProcessor;
import no.cantara.docsite.web.WebContext;
import no.cantara.docsite.web.WebHandler;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ContentsHandler implements WebHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ContentsHandler.class);

    @Override
    public boolean handleRequest(DynamicConfiguration configuration, CacheStore cacheStore, ResourceContext resourceContext, WebContext webContext, HttpServerExchange exchange) {
        Map<String, Object> templateVariables = new HashMap<>();

        if (resourceContext.getTuples().size() != 2) {
            return false;
        }

        ScmRepositoryContentsService repositoryContentsService = new ScmRepositoryContentsService(cacheStore);
        CacheKey cacheKey = CacheKey.of(cacheStore.getRepositoryConfig().getOrganization(RepoConfig.ScmProvider.GITHUB), resourceContext.getTuples().get(0).id, resourceContext.getTuples().get(1).resource);
        ScmRepositoryContents contents = repositoryContentsService.get(cacheKey);

        if (contents == null) {
            LOG.error("Contents is NULL. Probably because it was not fetched due to rate limit issue!");
            return false;
        }

        templateVariables.put("contents", contents);

        if (ThymeleafViewEngineProcessor.processView(exchange, cacheStore, webContext.asTemplateResource(), templateVariables)) {
            return true;
        }

        return false;
    }
}
