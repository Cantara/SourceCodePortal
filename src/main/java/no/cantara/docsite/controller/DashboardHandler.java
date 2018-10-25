package no.cantara.docsite.controller;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheShaKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepositoryConfig;
import no.cantara.docsite.domain.github.commits.CommitRevision;
import no.cantara.docsite.domain.view.DashboardModel;
import no.cantara.docsite.web.ResourceContext;
import no.cantara.docsite.web.ThymeleafViewEngineProcessor;
import no.cantara.docsite.web.WebContext;
import no.cantara.docsite.web.WebHandler;
import no.ssb.config.DynamicConfiguration;

import javax.cache.Cache;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static no.cantara.docsite.controller.CommitsHandler.sortByValue;

public class DashboardHandler implements WebHandler {

    @Override
    public boolean handleRequest(DynamicConfiguration configuration, CacheStore cacheStore, ResourceContext resourceContext, WebContext webContext, HttpServerExchange exchange) {
        Map<String, Object> templateVariables = new HashMap<>();

        DashboardModel model = new DashboardModel();

        // TODO this is bit expensive per view. Investigate how JCache can provide a sorted tree map
        {
            Cache<CacheShaKey, CommitRevision> commitRevisions = cacheStore.getCommits();
            Map<CacheShaKey, CommitRevision> commitRevisionMap = new LinkedHashMap<>();
            commitRevisions.iterator().forEachRemaining(a -> commitRevisionMap.put(a.getKey(), a.getValue()));
            Map<CacheShaKey, CommitRevision> sortedMap = sortByValue(commitRevisionMap);

            int n = 0;
            for (CommitRevision commitRevision : sortedMap.values()) {
                if (n > configuration.evaluateToInt("render.max.group.commits")) break;
                model.lastCommitRevisions.add(commitRevision);
                n++;
            }
        }

        for (RepositoryConfig.Repo repo : cacheStore.getGroups()) {
            boolean hasReadme = (repo.defaultGroupRepo != null && !"".equals(repo.defaultGroupRepo));
            DashboardModel.Group group = new DashboardModel.Group(
                    cacheStore.getRepositoryConfig().gitHub.organization,
                    repo.repo,
                    repo.defaultGroupRepo,
                    repo.branch,
                    repo.groupId,
                    repo.displayName,
                    repo.description,
                    hasReadme,
                    String.format("/contents/%s/%s", repo.defaultGroupRepo, repo.branch),
                    String.format("/group/%s", repo.groupId),
                    String.format(cacheStore.getRepositoryConfig().gitHub.badges.jenkins, repo.defaultGroupRepo),
                    String.format(cacheStore.getRepositoryConfig().gitHub.badges.snykIO, cacheStore.getRepositoryConfig().gitHub.organization, repo.defaultGroupRepo));

            group.setNoOfRepos(cacheStore.getRepositoryGroupsByGroupId(repo.groupId).size());


//            model.groups.add(group);
//            {
//                AtomicInteger count = new AtomicInteger(0);
//                cacheStore.getRepositoryGroups().iterator().forEachRemaining(a -> count.incrementAndGet());
//                model.connectedRepos = String.valueOf(count.get());
//            }
        }


//        DashboardModel.Activity activity = new DashboardModel.Activity();
//        group.activity.add(activity);


        templateVariables.put("model", model);

        if (ThymeleafViewEngineProcessor.processView(exchange, cacheStore, webContext.asTemplateResource(), templateVariables)) {
            return true;
        }

        return false;
    }
}
