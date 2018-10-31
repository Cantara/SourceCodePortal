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

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
        Set<ScmRepository> repos = repositoryGroups.getValue().stream().sorted(Comparator.comparing(c -> c.cacheRepositoryKey.repoName.toLowerCase())).collect(Collectors.toCollection(LinkedHashSet::new));
        ScmRepositoryGroup<Set<ScmRepository>> scmRepositoryGroup = new ScmRepositoryGroup<>(repos, repositoryConfig.displayName, repositoryConfig.description, repos.size());
        templateVariables.put("lastCommitRevisions", lastCommitRevisions);
        templateVariables.put("cacheKey", key);
        templateVariables.put("repositoryGroup", scmRepositoryGroup);

        if (ThymeleafViewEngineProcessor.processView(exchange, cacheStore, webContext.asTemplateResource(), templateVariables)) {
            return true;
        }

        return false;
    }

}
