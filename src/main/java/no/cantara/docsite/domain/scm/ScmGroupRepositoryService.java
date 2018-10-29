package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheHelper;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheService;
import no.cantara.docsite.cache.CacheStore;

import javax.cache.Cache;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ScmGroupRepositoryService implements CacheService<CacheRepositoryKey, ScmGroupRepository> {

    private final CacheStore cacheStore;

    public ScmGroupRepositoryService(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    public ScmGroupRepository get(CacheRepositoryKey key) {
        return cacheStore.getRepositoryGroup().get(key);
    }

    @Override
    public Iterator<Cache.Entry<CacheRepositoryKey, ScmGroupRepository>> getAll() {
        return cacheStore.getRepositoryGroup().iterator();
    }

    @Override
    public Set<CacheRepositoryKey> keySet() {
        return StreamSupport.stream(cacheStore.getRepositoryGroup().spliterator(), false).map(m -> m.getKey()).collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(c -> c.groupId))));
    }

    @Override
    public Map<CacheRepositoryKey, ScmGroupRepository> entrySet() {
        return StreamSupport.stream(cacheStore.getRepositoryGroup().spliterator(), false)
                .sorted(Comparator.comparing(entry -> entry.getKey().groupId))
                .collect(Collectors.toMap(Cache.Entry::getKey, Cache.Entry::getValue, (oldValue, newValue) -> newValue, () -> new TreeMap<>(Comparator.comparing(c -> c.repoName))));
    }

    @Override
    public long size() {
        return CacheHelper.cacheSize(cacheStore.getRepositoryGroup());
    }
}
