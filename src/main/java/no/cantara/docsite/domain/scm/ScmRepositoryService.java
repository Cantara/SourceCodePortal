package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheGroupKey;
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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;

public class ScmRepositoryService implements CacheService<CacheRepositoryKey, ScmRepository> {

    private final CacheStore cacheStore;

    public ScmRepositoryService(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    /**
     * Get a concrete repository
     *
     * @param key Identifier
     * @return Repository definition that is not bound to any specific scm
     */
    @Override
    public ScmRepository get(CacheRepositoryKey key) {
        return cacheStore.getRepositories().get(key);
    }

    /**
     * Get all repositories
     *
     * @return A cache iterator
     */
    @Override
    public Iterator<Cache.Entry<CacheRepositoryKey, ScmRepository>> getAll() {
        return cacheStore.getRepositories().iterator();
    }

    public Set<CacheGroupKey> groupKeySet() {
        TreeSet<CacheGroupKey> keySet = new TreeSet<>(Comparator.comparing(o -> o.groupId));
        cacheStore.getCacheGroupKeys().iterator().forEachRemaining(entry -> keySet.add(entry.getKey()));
        return keySet;
    }

    @Override
    public Set<CacheRepositoryKey> keySet() {
        return StreamSupport.stream(cacheStore.getRepositories().spliterator(), false).map(m -> m.getKey()).collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(c -> c.groupId))));
    }

    @Override
    public Map<CacheRepositoryKey, ScmRepository> entrySet() {
        return StreamSupport.stream(cacheStore.getRepositories().spliterator(), false)
                .sorted(Comparator.comparing(entry -> entry.getKey().groupId))
                .collect(Collectors.toMap(Cache.Entry::getKey, Cache.Entry::getValue, (oldValue, newValue) -> newValue, () -> new TreeMap<>(Comparator.comparing(c -> c.repoName))));
    }

    @Override
    public long size() {
        return CacheHelper.cacheSize(cacheStore.getRepositories());
    }

    public Map<CacheGroupKey, Set<ScmRepository>> sortedEntrySet() {
        Stream<Cache.Entry<CacheRepositoryKey, ScmRepository>> stream = StreamSupport.stream(cacheStore.getRepositories().spliterator(), false);
        return stream
                .sorted(Comparator.comparing(entry -> entry.getKey().groupId))
                .collect(Collectors.toMap(Cache.Entry::getKey, Cache.Entry::getValue, (oldValue, newValue) -> newValue, () -> new TreeMap<>(Comparator.comparing(c -> c.repoName))))
                .values().stream()
                .collect(groupingBy(ScmRepository::getCacheGroupKey, Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(c -> c.cacheRepositoryKey.repoName)))));
    }

}
