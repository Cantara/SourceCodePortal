package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheGroupKey;
import no.cantara.docsite.cache.CacheService;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.Repository;

import javax.cache.Cache;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RepositoryService implements CacheService<CacheGroupKey, Repository> {

    private final CacheStore cacheStore;

    public RepositoryService(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    public Repository get(CacheGroupKey key) {
        return cacheStore.getRepositoryGroups().get(key);
    }

    @Override
    public Iterator<Cache.Entry<CacheGroupKey, Repository>> getAll() {
        return cacheStore.getRepositoryGroups().iterator();
    }

    @Override
    public Set<CacheGroupKey> keySet() {
        return StreamSupport.stream(cacheStore.getCacheGroupKeys().spliterator(), false).map(m -> m.getKey()).collect(Collectors.toSet());
    }

    @Override
    public Map<CacheGroupKey, Repository> entrySet() {
        return StreamSupport.stream(cacheStore.getRepositoryGroups().spliterator(), false)
                .collect(Collectors.toMap(Cache.Entry::getKey, Cache.Entry::getValue, (oldValue, newValue) -> newValue, TreeMap::new));
    }

}
