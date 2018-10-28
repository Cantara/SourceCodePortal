package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheService;
import no.cantara.docsite.cache.CacheStore;

import javax.cache.Cache;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RepositoryService implements CacheService<CacheRepositoryKey, RepositoryDefinition> {

    private final CacheStore cacheStore;

    public RepositoryService(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    // The CacheGroupKey should possible be renamed to CacheRepositoryKey and a GroupKey should only be a group
    @Override
    public RepositoryDefinition get(CacheRepositoryKey key) {
        return cacheStore.getRepositories().get(key);
    }

    @Override
    public Iterator<Cache.Entry<CacheRepositoryKey, RepositoryDefinition>> getAll() {
        return cacheStore.getRepositories().iterator();
    }

    @Override
    public Set<CacheRepositoryKey> keySet() {
        return StreamSupport.stream(cacheStore.getCacheRepositoryKeys().spliterator(), false).map(m -> m.getKey()).collect(Collectors.toSet());
    }

    @Override
    public Map<CacheRepositoryKey, RepositoryDefinition> entrySet() {
        return StreamSupport.stream(cacheStore.getRepositories().spliterator(), false)
                .collect(Collectors.toMap(Cache.Entry::getKey, Cache.Entry::getValue, (oldValue, newValue) -> newValue, TreeMap::new));
    }

}
