package no.cantara.docsite.controller.handler;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheShaKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.github.commits.GroupByDateIterator;
import no.cantara.docsite.domain.scm.ScmCommitRevision;
import no.cantara.docsite.domain.view.CommitRevisionsModel;
import no.cantara.docsite.web.ResourceContext;
import no.cantara.docsite.web.ThymeleafViewEngineProcessor;
import no.cantara.docsite.web.WebContext;
import no.cantara.docsite.web.WebHandler;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CommitsHandler implements WebHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CommitsHandler.class);

    public static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (o1, o2) -> {
            ScmCommitRevision m1 = (ScmCommitRevision) o1.getValue();
            ScmCommitRevision m2 = (ScmCommitRevision) o2.getValue();
            return (m2.date.compareTo(m1.date));
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<K, V> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }


    @Override
    public boolean handleRequest(DynamicConfiguration configuration, CacheStore cacheStore, ResourceContext resourceContext, WebContext webContext, HttpServerExchange exchange) {
        try {
            Map<String, Object> templateVariables = new HashMap<>();

            if (resourceContext.getTuples().size() > 2) {
                return false;
            }

            boolean renderAll = (resourceContext.getTuples().size() == 1 && resourceContext.getLast().get().id == null);

            Map<CacheShaKey, ScmCommitRevision> commitRevisionMap = new LinkedHashMap<>();
            Cache<CacheShaKey, ScmCommitRevision> commitRevisions = cacheStore.getCommits();
            if (renderAll) {
                // all view
                commitRevisions.iterator().forEachRemaining(a -> commitRevisionMap.put(a.getKey(), a.getValue()));
                templateVariables.put("displayName", String.format("All %s repos", cacheStore.getRepositoryConfig().gitHub.organization));

            } else {
                boolean renderGroupOrRepo = resourceContext.getTuples().size() == 1;

                String organization = cacheStore.getRepositoryConfig().gitHub.organization;
                String groupIdOrRepoName = (renderGroupOrRepo ? resourceContext.getLast().get().id : resourceContext.getFirst().get().id);
                String branchOrNull = (renderGroupOrRepo ? null : resourceContext.getLast().get().resource);

                templateVariables.put("displayName", groupIdOrRepoName);

                String groupIdIfRenderRepo = null;
                if (!renderGroupOrRepo) {
                    CacheRepositoryKey cacheRepositoryKey = cacheStore.getCacheRepositoryKey(CacheKey.of(organization, groupIdOrRepoName, branchOrNull));
                    if (cacheRepositoryKey != null) {
                        groupIdIfRenderRepo = cacheRepositoryKey.groupId;
                    }
                }

                for (Cache.Entry<CacheShaKey, ScmCommitRevision> entry : commitRevisions) {
                    CacheShaKey key = entry.getKey();
                    ScmCommitRevision value = entry.getValue();
                    // group view
                    if (renderGroupOrRepo && key.compareToUsingGroupId(organization, groupIdOrRepoName)) {
                        commitRevisionMap.put(key, value);

                    // repo view
                    } else if (!renderGroupOrRepo && key.compareToUsingRepoName(organization, groupIdOrRepoName, branchOrNull, groupIdIfRenderRepo)) {
                        commitRevisionMap.put(key, value);
                    }
                }
            }

            Map<CacheShaKey, ScmCommitRevision> sortedMap = sortByValue(commitRevisionMap);
            GroupByDateIterator groupByDateIterator = new GroupByDateIterator(new ArrayList<>(sortedMap.values()));

            CommitRevisionsModel model = new CommitRevisionsModel(groupByDateIterator);

            templateVariables.put("model", model);

            if (ThymeleafViewEngineProcessor.processView(exchange, cacheStore, webContext.asTemplateResource(), templateVariables)) {
                return true;
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
