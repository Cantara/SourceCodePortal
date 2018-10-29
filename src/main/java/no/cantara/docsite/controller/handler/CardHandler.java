package no.cantara.docsite.controller.handler;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheShaKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepositoryConfigBinding;
import no.cantara.docsite.domain.scm.ScmCommitRevision;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.cantara.docsite.domain.view.CardModel;
import no.cantara.docsite.domain.view.DashboardModel;
import no.cantara.docsite.web.ResourceContext;
import no.cantara.docsite.web.ThymeleafViewEngineProcessor;
import no.cantara.docsite.web.WebContext;
import no.cantara.docsite.web.WebHandler;
import no.ssb.config.DynamicConfiguration;

import javax.cache.Cache;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static no.cantara.docsite.controller.handler.CommitsHandler.sortByValue;

public class CardHandler implements WebHandler {

    @Override
    public boolean handleRequest(DynamicConfiguration configuration, CacheStore cacheStore, ResourceContext resourceContext, WebContext webContext, HttpServerExchange exchange) {
        Map<String, Object> templateVariables = new HashMap<>();

        RepositoryConfigBinding.Repo repositoryConfig = cacheStore.getGroupByGroupId(resourceContext.getLast().get().id);
        if (repositoryConfig == null) {
            return false;
        }

        templateVariables.put("repositoryConfig", repositoryConfig);
        List<ScmRepository> repositories = cacheStore.getRepositoryGroupsByGroupId(repositoryConfig.groupId);
        templateVariables.put("repositoryGroup", repositories);

        CardModel model = new CardModel();

        // TODO this is bit expensive per view. Investigate how JCache can provide a sorted tree map
        {
            Cache<CacheShaKey, ScmCommitRevision> commitRevisions = cacheStore.getCommits();
            Map<CacheShaKey, ScmCommitRevision> commitRevisionMap = new LinkedHashMap<>();
            for(Cache.Entry<CacheShaKey, ScmCommitRevision> commitRevision : commitRevisions) {
                if (commitRevision.getKey().groupId.equals(repositoryConfig.groupId)) {
                    commitRevisionMap.put(commitRevision.getKey(), commitRevision.getValue());
                }
            }
            Map<CacheShaKey, ScmCommitRevision> sortedMap = sortByValue(commitRevisionMap);

            int n = 0;
            for (ScmCommitRevision scmCommitRevision : sortedMap.values()) {
                if (n > configuration.evaluateToInt("render.max.group.commits")) break;
                model.lastCommitRevisions.add(scmCommitRevision);
                n++;
            }
        }

        for(ScmRepository repo : repositories) {
            boolean hasReadme = cacheStore.getReadmeContents().containsKey(repo.cacheRepositoryKey.asCacheKey());
            DashboardModel.Group group = new DashboardModel.Group(
                    cacheStore.getRepositoryConfig().gitHub.organization,
                    repo.cacheRepositoryKey.repoName,
                    repositoryConfig.defaultGroupRepo,
                    repo.cacheRepositoryKey.branch,
                    repositoryConfig.groupId,
                    repo.description,
                    repo.description,
                    hasReadme,
                    (hasReadme ? String.format("/contents/%s/%s", repo.cacheRepositoryKey.repoName, repo.cacheRepositoryKey.branch) : repo.repoURL.getExternalURL()),
                    String.format("/group/%s", repositoryConfig.groupId),
                    repo.repoURL.getExternalURL());
            model.groups.add(group);
        }

        templateVariables.put("model", model);

        if (ThymeleafViewEngineProcessor.processView(exchange, cacheStore, webContext.asTemplateResource(), templateVariables)) {
            return true;
        }

        return false;
    }

}
