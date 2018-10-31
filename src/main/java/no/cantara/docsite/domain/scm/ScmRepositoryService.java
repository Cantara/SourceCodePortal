package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheGroupKey;
import no.cantara.docsite.cache.CacheHelper;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheService;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepositoryConfigBinding;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
                .collect(Collectors.toMap(Cache.Entry::getKey, Cache.Entry::getValue, (oldValue, newValue) -> newValue, () -> new TreeMap<>(Comparator.comparing(c -> c.repoName))))
                .values().stream()
                .sorted(Comparator.comparing(entry -> entry.cacheRepositoryKey.groupId))
                .collect(groupingBy(ScmRepository::getCacheGroupKey, Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(c -> c.cacheRepositoryKey.repoName)))));
    }

    public Map<CacheRepositoryKey, Set<ScmRepository>> sortedGroupedEntrySet() {
        Stream<Cache.Entry<CacheRepositoryKey, ScmRepository>> stream = StreamSupport.stream(cacheStore.getRepositories().spliterator(), false);
        return stream
                .collect(Collectors.toMap(Cache.Entry::getKey, Cache.Entry::getValue, (oldValue, newValue) -> newValue, () -> new TreeMap<>(Comparator.comparing(c -> c.repoName))))
                .values().stream()
                .sorted(Comparator.comparing(entry -> entry.cacheRepositoryKey.groupId))
                .collect(groupingBy(ScmRepository::getCacheRepositoryKey, Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(c -> c.cacheRepositoryKey.repoName)))));
    }

    private List<RepositoryConfigBinding.Repo> getGroups() {
        List<RepositoryConfigBinding.Repo> groups = new ArrayList<>();
        cacheStore.getRepositoryConfig().gitHub.repos.forEach(repo -> {
            groups.add(repo);
        });
        return groups;
    }

    private Set<ScmRepository> getRepositoryGroupsByGroupId(String groupId) {
        Set<ScmRepository> repositories = new LinkedHashSet<>();
        cacheStore.getRepositories().forEach(a -> {
            if (a.getKey().compareTo(groupId)) {
                repositories.add(a.getValue());
            }
        });
        return repositories;
    }

    public Map<CacheRepositoryKey, Set<ScmRepository>> groupedRepositories() {
        Map<CacheRepositoryKey, Set<ScmRepository>> groupedRepositories = new LinkedHashMap<>();
        for(RepositoryConfigBinding.Repo repo : getGroups()) {
            // create default repo key
            CacheRepositoryKey cacheRepositoryKey = CacheRepositoryKey.of(cacheStore.getRepositoryConfig().gitHub.organization, repo.defaultGroupRepo, repo.branch, repo.groupId, true);
            Set<ScmRepository> repositories = getRepositoryGroupsByGroupId(repo.groupId);
            groupedRepositories.put(cacheRepositoryKey, repositories);
        }
        return groupedRepositories;
    }

    public Map<CacheRepositoryKey, ScmRepositoryGroup<ScmRepository>> defaultRepositoryGroups() {
        Map<CacheRepositoryKey, ScmRepositoryGroup<ScmRepository>> groupedRepositories = new LinkedHashMap<>();
        for(RepositoryConfigBinding.Repo repo : getGroups()) {
            CacheRepositoryKey cacheRepositoryKey = CacheRepositoryKey.of(cacheStore.getRepositoryConfig().gitHub.organization, repo.defaultGroupRepo, repo.branch, repo.groupId, true);
            ScmRepository scmRepository = entrySet().get(cacheRepositoryKey);
            int numberOfRepos = getRepositoryGroupsByGroupId(cacheRepositoryKey.groupId).size();
            String displayName = repo.displayName;
            String description = repo.description;
            groupedRepositories.put(cacheRepositoryKey, new ScmRepositoryGroup<>(scmRepository, displayName, description, numberOfRepos));
        }
        return groupedRepositories;
    }

    public RepositoryConfigBinding.Repo getGroupRepoConfig(String groupId) {
        RepositoryConfigBinding.Repo groupRepo = null;
        for(RepositoryConfigBinding.Repo repo : getGroups()) {
            if (groupId.equals(repo.groupId)) {
                return repo;
            }
        }
        return null;
    }

    public Map.Entry<CacheRepositoryKey, Set<ScmRepository>> getRepositoryGroups(String groupId) {
        for(Map.Entry<CacheRepositoryKey, Set<ScmRepository>> group : sortedGroupedEntrySet().entrySet()) {
            if (groupId.equals(group.getKey().groupId)) {
                return group;
            }
        }
        return null;
    }

}
