package no.cantara.docsite.controller.handler;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.Repository;
import no.cantara.docsite.domain.config.RepositoryConfigBinding;
import no.cantara.docsite.domain.github.contents.RepositoryContentsBinding;
import no.cantara.docsite.domain.view.ContentsModel;
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

        CacheKey cacheKey = CacheKey.of(cacheStore.getRepositoryConfig().gitHub.organization, resourceContext.getTuples().get(0).id, resourceContext.getTuples().get(1).resource);

        RepositoryContentsBinding contents = cacheStore.getPages().get(cacheKey);

        if (contents == null) {
            LOG.error("Contents is NULL. Probably because it was not fetched due to rate limit issue!");
            return false;
        }

        CacheRepositoryKey cacheRepositoryKey = cacheStore.getCacheRepositoryKey(cacheKey);
        Repository repository = cacheStore.getRepositoryGroups().get(cacheRepositoryKey);

        ContentsModel model = new ContentsModel(repository, contents, contents.renderedHtml);

        for (RepositoryConfigBinding.Repo repo : cacheStore.getGroups()) {
            boolean hasReadme = (repo.defaultGroupRepo != null && !"".equals(repo.defaultGroupRepo));
            ContentsModel.Group group = new ContentsModel.Group(
                    cacheStore.getRepositoryConfig().gitHub.organization,
                    repo.repo,
                    repo.branch,
                    repo.groupId,
                    repo.displayName,
                    repo.description,
                    hasReadme,
                    String.format("/contents/%s/%s", repo.repo, repo.branch),
                    String.format("/group/%s", repo.groupId));
            model.groups.add(group);
        }

        templateVariables.put("model", model);

        if (ThymeleafViewEngineProcessor.processView(exchange, cacheStore, webContext.asTemplateResource(), templateVariables)) {
            return true;
        }

        return false;
    }
}
