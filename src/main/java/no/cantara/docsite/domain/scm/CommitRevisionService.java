package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheService;
import no.cantara.docsite.cache.CacheShaKey;
import no.cantara.docsite.cache.CacheStore;

import javax.cache.Cache;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CommitRevisionService implements CacheService<CacheShaKey, CommitRevision> {

    private final CacheStore cacheStore;

    public CommitRevisionService(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    public CommitRevision get(CacheShaKey key) {
        return cacheStore.getCommits().get(key);
    }

    @Override
    public Iterator<Cache.Entry<CacheShaKey, CommitRevision>> getAll() {
        return cacheStore.getCommits().iterator();
    }

    @Override
    public Set<CacheShaKey> keySet() {
        return StreamSupport.stream(cacheStore.getCommits().spliterator(), false).map(m -> m.getKey()).collect(Collectors.toSet());
    }

    @Override
    public Map<CacheShaKey, CommitRevision> entrySet() {
        return StreamSupport.stream(cacheStore.getCommits().spliterator(), false).collect(Collectors.toMap(Cache.Entry::getKey, Cache.Entry::getValue));
    }
}
