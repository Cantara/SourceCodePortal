package no.cantara.docsite.controller.handler;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.scm.ScmCommitRevision;
import no.cantara.docsite.domain.scm.ScmCommitRevisionService;
import no.cantara.docsite.domain.scm.ScmGroupRepository;
import no.cantara.docsite.domain.scm.ScmGroupRepositoryService;
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

        ScmGroupRepositoryService groupRepositoryService = new ScmGroupRepositoryService(cacheStore);
        Map<CacheRepositoryKey, ScmGroupRepository> entrySet = groupRepositoryService.entrySet();
        templateVariables.put("groupedRepos", entrySet);

//        for (RepositoryConfigBinding.Repo repo : cacheStore.getGroups()) {
//            boolean hasReadme = (repo.defaultGroupRepo != null && !"".equals(repo.defaultGroupRepo));
//            DashboardModel.Group group = new DashboardModel.Group(
//                    cacheStore.getRepositoryConfig().gitHub.organization,
//                    repo.repo,
//                    repo.defaultGroupRepo,
//                    repo.branch,
//                    repo.groupId,
//                    repo.displayName,
//                    repo.description,
//                    hasReadme,
//                    String.format("/contents/%s/%s", repo.defaultGroupRepo, repo.branch),
//                    String.format("/group/%s", repo.groupId),
//                    String.format("https://github.com/%s/%s", cacheStore.getRepositoryConfig().gitHub.organization, repo.defaultGroupRepo),
//                    String.format(cacheStore.getRepositoryConfig().gitHub.badges.jenkins, repo.defaultGroupRepo),
//                    String.format(cacheStore.getRepositoryConfig().gitHub.badges.snykIO, cacheStore.getRepositoryConfig().gitHub.organization, repo.defaultGroupRepo));
//
//            group.setNoOfRepos(cacheStore.getRepositoryGroupsByGroupId(repo.groupId).size());
//
//            model.groups.add(group);

//            {
//                AtomicInteger count = new AtomicInteger(0);
//                cacheStore.getRepositoryGroups().iterator().forEachRemaining(a -> count.incrementAndGet());
//                model.connectedRepos = String.valueOf(count.get());
//            }
//        }


//        DashboardModel.Activity activity = new DashboardModel.Activity();
//        group.activity.add(activity);


//        templateVariables.put("model", model);

        if (ThymeleafViewEngineProcessor.processView(exchange, cacheStore, webContext.asTemplateResource(), templateVariables)) {
            return true;
        }

        return false;
    }
}
