package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheGroupKey;
import no.cantara.docsite.cache.CacheService;
import no.cantara.docsite.cache.CacheStore;

import javax.cache.Cache;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RepositoryService implements CacheService<CacheGroupKey, RepositoryDefinition> {

    private final CacheStore cacheStore;

    public RepositoryService(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    public RepositoryDefinition get(CacheGroupKey key) {
        return cacheStore.getRepositories().get(key);
    }

    @Override
    public Iterator<Cache.Entry<CacheGroupKey, RepositoryDefinition>> getAll() {
        return cacheStore.getRepositories().iterator();
    }

    @Override
    public Set<CacheGroupKey> keySet() {
        return StreamSupport.stream(cacheStore.getCacheGroupKeys().spliterator(), false).map(m -> m.getKey()).collect(Collectors.toSet());
    }

    @Override
    public Map<CacheGroupKey, RepositoryDefinition> entrySet() {
        return StreamSupport.stream(cacheStore.getRepositories().spliterator(), false)
                .collect(Collectors.toMap(Cache.Entry::getKey, Cache.Entry::getValue, (oldValue, newValue) -> newValue, TreeMap::new));
    }

}
