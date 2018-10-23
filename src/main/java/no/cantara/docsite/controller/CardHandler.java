package no.cantara.docsite.controller;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.Repository;
import no.cantara.docsite.domain.config.RepositoryConfig;
import no.cantara.docsite.domain.view.CardModel;
import no.cantara.docsite.domain.view.DashboardModel;
import no.cantara.docsite.web.ResourceContext;
import no.cantara.docsite.web.ThymeleafViewEngineProcessor;
import no.cantara.docsite.web.WebContext;
import no.cantara.docsite.web.WebHandler;
import no.ssb.config.DynamicConfiguration;

import java.util.HashMap;
import java.util.List;
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
        List<Repository> repositories = cacheStore.getRepositoryGroupsByGroupId(repositoryConfig.groupId);
        templateVariables.put("repositoryGroup", repositories);

        CardModel model = new CardModel();

        for(Repository repo : repositories) {
            boolean hasReadme = cacheStore.getPages().containsKey(repo.cacheKey);
            DashboardModel.Group group = new DashboardModel.Group(
                    cacheStore.getRepositoryConfig().gitHub.organization,
                    repo.cacheKey.repoName,
                    repo.cacheKey.branch,
                    repositoryConfig.groupId,
                    repo.description,
                    repo.description,
                    hasReadme,
                    (hasReadme ? String.format("/contents/%s/%s", repo.cacheKey.repoName, repo.cacheKey.branch) : repo.repoURL),
                    String.format("/group/%s", repositoryConfig.groupId),
                    String.format(cacheStore.getRepositoryConfig().gitHub.badges.jenkins, repo.cacheKey.repoName),
                    String.format(cacheStore.getRepositoryConfig().gitHub.badges.snykIO, cacheStore.getRepositoryConfig().gitHub.organization, repo.cacheKey.repoName));
            model.groups.add(group);
        }

        templateVariables.put("model", model);

        if (ThymeleafViewEngineProcessor.processView(exchange, webContext.asTemplateResource(), templateVariables)) {
            return true;
        }

        return false;
    }

}
