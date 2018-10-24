package no.cantara.docsite.controller;

import io.undertow.server.HttpServerExchange;
import no.cantara.docsite.cache.CacheGroupKey;
import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheShaKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.github.commits.CommitRevision;
import no.cantara.docsite.domain.github.commits.GroupByDateIterator;
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
            CommitRevision m1 = (CommitRevision) o1.getValue();
            CommitRevision m2 = (CommitRevision) o2.getValue();
            return (m2.commit.commitAuthor.date.compareTo(m1.commit.commitAuthor.date));
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

            Map<CacheShaKey, CommitRevision> commitRevisionMap = new LinkedHashMap<>();
            Cache<CacheShaKey, CommitRevision> commitRevisions = cacheStore.getCommits();
            if (renderAll) {
                commitRevisions.iterator().forEachRemaining(a -> commitRevisionMap.put(a.getKey(), a.getValue()));

            } else {
                boolean renderGroupOrRepo = resourceContext.getTuples().size() == 1;

                String organization = cacheStore.getRepositoryConfig().gitHub.organization;
                String groupIdOrRepoName = (renderGroupOrRepo ? resourceContext.getLast().get().id : resourceContext.getFirst().get().id);
                String branchOrNull = (renderGroupOrRepo ? null : resourceContext.getLast().get().resource);

                String groupIdIfRenderRepo = null;
                if (!renderGroupOrRepo) {
                    CacheGroupKey cacheGroupKey = cacheStore.getCacheGroupKey(CacheKey.of(organization, groupIdOrRepoName, branchOrNull));
                    if (cacheGroupKey != null) {
                        groupIdIfRenderRepo = cacheGroupKey.groupId;
                    }
                }

                for (Cache.Entry<CacheShaKey, CommitRevision> entry : commitRevisions) {
                    CacheShaKey key = entry.getKey();
                    CommitRevision value = entry.getValue();
                    if (renderGroupOrRepo && key.compareToUsingGroupId(organization, groupIdOrRepoName)) {
                        commitRevisionMap.put(key, value);

                    } else if (!renderGroupOrRepo && key.compareToUsingRepoName(organization, groupIdOrRepoName, branchOrNull, groupIdIfRenderRepo)) {
                        commitRevisionMap.put(key, value);
                    }
                }
            }

            Map<CacheShaKey, CommitRevision> sortedMap = sortByValue(commitRevisionMap);
            GroupByDateIterator groupByDateIterator = new GroupByDateIterator(new ArrayList<>(sortedMap.values()));

            CommitRevisionsModel model = new CommitRevisionsModel(groupByDateIterator);

            templateVariables.put("model", model);

            if (ThymeleafViewEngineProcessor.processView(exchange, webContext.asTemplateResource(), templateVariables)) {
                return true;
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
