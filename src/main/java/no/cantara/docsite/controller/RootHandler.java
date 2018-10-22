package no.cantara.docsite.controller;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepositoryConfig;
import no.cantara.docsite.domain.view.DashboardModel;
import no.cantara.docsite.web.ResourceContext;
import no.cantara.docsite.web.ThymeleafViewEngineProcessor;
import no.cantara.docsite.web.WebContext;
import no.cantara.docsite.web.WebHandler;
import no.ssb.config.DynamicConfiguration;

import java.util.HashMap;
import java.util.Map;

public class RootHandler implements WebHandler {

    @Override
    public boolean handleRequest(DynamicConfiguration configuration, CacheStore cacheStore, ResourceContext resourceContext, WebContext webContext, HttpServerExchange exchange) {
        Map<String, Object> templateVariables = new HashMap<>();

        DashboardModel model = new DashboardModel();

        for (RepositoryConfig.Repo repo : cacheStore.getGroups()) {
            boolean hasReadme = (repo.defaultRepo != null && !"".equals(repo.defaultRepo));
            DashboardModel.Group group = new DashboardModel.Group(
                    cacheStore.getRepositoryConfig().gitHub.organization,
                    repo.repo,
                    repo.branch,
                    repo.groupId,
                    repo.displayName,
                    repo.description,
                    hasReadme,
                    String.format("/contents/%s/%s", repo.defaultRepo, repo.branch),
                    String.format("/card/%s", repo.groupId),
                    String.format(cacheStore.getRepositoryConfig().gitHub.badges.jenkins, repo.defaultRepo),
                    String.format(cacheStore.getRepositoryConfig().gitHub.badges.snykIO, cacheStore.getRepositoryConfig().gitHub.organization, repo.defaultRepo));
            model.groups.add(group);
        }

//        DashboardModel.Activity activity = new DashboardModel.Activity();
//        group.activity.add(activity);

        templateVariables.put("model", model);

        if (ThymeleafViewEngineProcessor.processView(exchange, webContext.asTemplateResource(), templateVariables)) {
            return true;
        }

        return false;
    }
}
