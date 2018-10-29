package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheHelper;
import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheService;
import no.cantara.docsite.cache.CacheStore;

import javax.cache.Cache;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ScmRepositoryContentsService implements CacheService<CacheKey, ScmRepositoryContents> {

    private final CacheStore cacheStore;

    public ScmRepositoryContentsService(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    public ScmRepositoryContents get(CacheKey key) {
        return cacheStore.getReadmeContents().get(key);
    }

    @Override
    public Iterator<Cache.Entry<CacheKey, ScmRepositoryContents>> getAll() {
        return cacheStore.getReadmeContents().iterator();
    }

    @Override
    public Set<CacheKey> keySet() {
        return StreamSupport.stream(cacheStore.getReadmeContents().spliterator(), false).map(m -> m.getKey()).collect(Collectors.toSet());
    }

    @Override
    public Map<CacheKey, ScmRepositoryContents> entrySet() {
        return StreamSupport.stream(cacheStore.getReadmeContents().spliterator(), false).collect(Collectors.toMap(Cache.Entry::getKey, Cache.Entry::getValue));
    }

    @Override
    public long size() {
        return CacheHelper.cacheSize(cacheStore.getReadmeContents());
    }
}
