package no.cantara.docsite.controller.handler;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepositoryConfigBinding;
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
import java.util.Set;
import java.util.stream.Collectors;

public class CardHandler implements WebHandler {

    @Override
    public boolean handleRequest(DynamicConfiguration configuration, CacheStore cacheStore, ResourceContext resourceContext, WebContext webContext, HttpServerExchange exchange) {
        Map<String, Object> templateVariables = new HashMap<>();

        String groupId = resourceContext.getLast().get().id;
        ScmRepositoryService service = new ScmRepositoryService(cacheStore);
        RepositoryConfigBinding.Repo repositoryConfig = service.getGroupRepoConfig(groupId);
        if (repositoryConfig == null) {
            return false;
        }

        ScmCommitRevisionService commitRevisionService = new ScmCommitRevisionService(cacheStore);
        List<ScmCommitRevision> lastCommitRevisions = commitRevisionService.entrySet(groupId).stream().limit(5).collect(Collectors.toList());;
        templateVariables.put("lastCommitRevisions", lastCommitRevisions);

        Map.Entry<CacheRepositoryKey, Set<ScmRepository>> repositoryGroups = service.getRepositoryGroups(groupId);
        CacheRepositoryKey key = repositoryGroups.getKey();
        Set<ScmRepository> repos = repositoryGroups.getValue();

        ScmRepositoryGroup<Set<ScmRepository>> scmRepositoryGroup = new ScmRepositoryGroup<Set<ScmRepository>>(repos, repositoryConfig.displayName, repositoryConfig.description, repos.size());
        Map.Entry<CacheRepositoryKey, ScmRepositoryGroup<Set<ScmRepository>>> repositoryGroupEntry = new Map.Entry<>() {
            @Override
            public CacheRepositoryKey getKey() {
                return key;
            }

            @Override
            public ScmRepositoryGroup<Set<ScmRepository>> getValue() {
                return scmRepositoryGroup;
            }

            @Override
            public ScmRepositoryGroup<Set<ScmRepository>> setValue(ScmRepositoryGroup<Set<ScmRepository>> value) {
                throw new UnsupportedOperationException("Not supported");
            }
        };

        templateVariables.put("repositoryGroups", repositoryGroupEntry);



//        templateVariables.put("repositoryConfig", repositoryConfig);
//        List<ScmRepository> repositories = cacheStore.getRepositoryGroupsByGroupId(repositoryConfig.groupId);
//        templateVariables.put("repositoryGroup", repositories);

//        CardModel model = new CardModel();

//        // TODO this is bit expensive per view. Investigate how JCache can provide a sorted tree map
//        {
//            Cache<CacheShaKey, ScmCommitRevision> commitRevisions = cacheStore.getCommits();
//            Map<CacheShaKey, ScmCommitRevision> commitRevisionMap = new LinkedHashMap<>();
//            for(Cache.Entry<CacheShaKey, ScmCommitRevision> commitRevision : commitRevisions) {
//                if (commitRevision.getKey().groupId.equals(repositoryConfig.groupId)) {
//                    commitRevisionMap.put(commitRevision.getKey(), commitRevision.getValue());
//                }
//            }
//            Map<CacheShaKey, ScmCommitRevision> sortedMap = sortByValue(commitRevisionMap);
//
//            int n = 0;
//            for (ScmCommitRevision scmCommitRevision : sortedMap.values()) {
//                if (n > configuration.evaluateToInt("render.max.group.commits")) break;
//                model.lastCommitRevisions.add(scmCommitRevision);
//                n++;
//            }
//        }

//        for(ScmRepository repo : repositories) {
//            boolean hasReadme = cacheStore.getReadmeContents().containsKey(repo.cacheRepositoryKey.asCacheKey());
//            DashboardModel.Group group = new DashboardModel.Group(
//                    cacheStore.getRepositoryConfig().gitHub.organization,
//                    repo.cacheRepositoryKey.repoName,
//                    repositoryConfig.defaultGroupRepo,
//                    repo.cacheRepositoryKey.branch,
//                    repositoryConfig.groupId,
//                    repo.description,
//                    repo.description,
//                    hasReadme,
//                    (hasReadme ? String.format("/contents/%s/%s", repo.cacheRepositoryKey.repoName, repo.cacheRepositoryKey.branch) : repo.repoURL.getExternalURL()),
//                    String.format("/group/%s", repositoryConfig.groupId),
//                    repo.repoURL.getExternalURL());
//            model.groups.add(group);
//        }
//
//        templateVariables.put("model", model);

        if (ThymeleafViewEngineProcessor.processView(exchange, cacheStore, webContext.asTemplateResource(), templateVariables)) {
            return true;
        }

        return false;
    }

}
