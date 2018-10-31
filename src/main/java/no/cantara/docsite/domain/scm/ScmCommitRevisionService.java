package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheHelper;
import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheService;
import no.cantara.docsite.cache.CacheShaKey;
import no.cantara.docsite.cache.CacheStore;

import javax.cache.Cache;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ScmCommitRevisionService implements CacheService<CacheShaKey, ScmCommitRevision> {

    private final CacheStore cacheStore;

    public ScmCommitRevisionService(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    public ScmCommitRevision get(CacheShaKey key) {
        return cacheStore.getCommits().get(key);
    }

    @Override
    public Iterator<Cache.Entry<CacheShaKey, ScmCommitRevision>> getAll() {
        return cacheStore.getCommits().iterator();
    }

    @Override
    public Set<CacheShaKey> keySet() {
        return StreamSupport.stream(cacheStore.getCommits().spliterator(), false).map(m -> m.getKey()).collect(Collectors.toSet());
    }

    @Override
    public Map<CacheShaKey, ScmCommitRevision> entrySet() {
        return StreamSupport.stream(cacheStore.getCommits().spliterator(), false)
                .sorted(Comparator.comparing(c -> c.getValue().date, Comparator.reverseOrder()))
                .collect(Collectors.toMap(Cache.Entry::getKey, Cache.Entry::getValue, (oldValue, newValue) -> newValue, LinkedHashMap::new));
    }

    public Set<ScmCommitRevision> entrySet(String groupId) {
        Map<CacheShaKey, ScmCommitRevision> entrySet = entrySet();
        Set<ScmCommitRevision> groupSet = entrySet.values().stream().filter(commitRevision -> groupId.equals(commitRevision.cacheShaKey.groupId)).collect(Collectors.toCollection(LinkedHashSet::new));
        return groupSet;
    }

    @Override
    public long size() {
        return CacheHelper.cacheSize(cacheStore.getCommits());
    }

    // returns the first found group key
    public CacheRepositoryKey getCacheRepositoryKey(CacheKey cacheKey) {
        Set<CacheRepositoryKey> groupKeys = StreamSupport.stream(cacheStore.getCacheRepositoryKeys().spliterator(), true)
                .filter(entry -> entry.getValue().equals(cacheKey))
                .map(Cache.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return groupKeys.stream().filter(entry -> entry.repoName.toLowerCase().contains(entry.groupId.toLowerCase())).findFirst()
                .orElse(groupKeys.iterator().hasNext() ? groupKeys.iterator().next() : null);
    }


}
