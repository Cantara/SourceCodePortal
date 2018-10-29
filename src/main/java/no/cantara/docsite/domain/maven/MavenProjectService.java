package no.cantara.docsite.domain.maven;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheService;
import no.cantara.docsite.cache.CacheStore;

import javax.cache.Cache;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MavenProjectService implements CacheService<CacheKey, MavenPOM> {

    private final CacheStore cacheStore;

    public MavenProjectService(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    public MavenPOM get(CacheKey key) {
        return cacheStore.getMavenProjects().get(key);
    }

    @Override
    public Iterator<Cache.Entry<CacheKey, MavenPOM>> getAll() {
        return cacheStore.getMavenProjects().iterator();
    }

    @Override
    public Set<CacheKey> keySet() {
        return StreamSupport.stream(cacheStore.getMavenProjects().spliterator(), false).map(m -> m.getKey()).collect(Collectors.toSet());
    }

    @Override
    public Map<CacheKey, MavenPOM> entrySet() {
        return StreamSupport.stream(cacheStore.getMavenProjects().spliterator(), false).collect(Collectors.toMap(Cache.Entry::getKey, Cache.Entry::getValue));
    }
}
