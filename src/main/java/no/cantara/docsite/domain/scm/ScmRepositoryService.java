package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.*;
import no.cantara.docsite.domain.config.RepoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;

public class ScmRepositoryService implements CacheService<CacheRepositoryKey, ScmRepository> {

    private static final Logger LOG = LoggerFactory.getLogger(ScmRepositoryService.class);
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

    private List<RepoConfig.Repo> getGroups() {
        return cacheStore.getRepositoryConfig().getConfig().repos.get(RepoConfig.ScmProvider.GITHUB);
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

    // TODO this is a workaround because of wrong modelling
    public ScmRepository getFirst(CacheKey cacheKey) {
        Set<CacheRepositoryKey> repo = cacheStore.getCacheRepositoryKeys(cacheKey);
        if (repo != null && repo.iterator().hasNext()) {
            CacheRepositoryKey cacheRepositoryKey = repo.iterator().next();
            return cacheStore.getRepositories().get(cacheRepositoryKey);
        }
        return null;
    }

    public Map<CacheRepositoryKey, Set<ScmRepository>> groupedRepositories() {
        Map<CacheRepositoryKey, Set<ScmRepository>> groupedRepositories = new LinkedHashMap<>();
        for (RepoConfig.Repo repo : getGroups()) {
            // create default repo key
            CacheRepositoryKey cacheRepositoryKey = CacheRepositoryKey.of(repo.organization, repo.defaultGroupRepo, repo.branchPattern, repo.groupId, true);
            Set<ScmRepository> repositories = getRepositoryGroupsByGroupId(repo.groupId);
            groupedRepositories.put(cacheRepositoryKey, repositories);
        }
        return groupedRepositories;
    }

    public Map<CacheRepositoryKey, ScmRepositoryGroup<ScmRepository>> defaultRepositoryGroups() {
        Map<CacheRepositoryKey, ScmRepositoryGroup<ScmRepository>> groupedRepositories = new LinkedHashMap<>();
        for (RepoConfig.Repo repo : getGroups().stream().sorted(Comparator.comparing(c -> c.groupId.toLowerCase())).collect(Collectors.toList())) {
            CacheRepositoryKey cacheRepositoryKey = CacheRepositoryKey.of(repo.organization, repo.defaultGroupRepo, repo.branchPattern, repo.groupId, true);
            ScmRepository scmRepository = entrySet().get(cacheRepositoryKey);
            int numberOfRepos = getRepositoryGroupsByGroupId(cacheRepositoryKey.groupId).size();
            String displayName = repo.displayName;
            String description = repo.description;
            if (scmRepository == null) {  // Skipping instead of breaking on config issues of repos
                LOG.error("Unable to resolve repository from configuration, repo:" + repo);
                LOG.error("You might want to check that the default-group-repo parameter in the configuration contain the repository name exists and is the one you want to represent the group ");
                //  throw new RuntimeException("Wrong: " + scmRepository + " => " + cacheRepositoryKey);
            } else {
                groupedRepositories.put(cacheRepositoryKey, new ScmRepositoryGroup<>(scmRepository, displayName, description, numberOfRepos));
            }
        }
        return groupedRepositories;
    }

    public RepoConfig.Repo getGroupRepoConfig(String groupId) {
        for (RepoConfig.Repo repo : getGroups()) {
            if (groupId.equals(repo.groupId)) {
                return repo;
            }
        }
        return null;
    }

    public Map.Entry<CacheRepositoryKey, Set<ScmRepository>> getRepositoryGroups(String groupId) {
        Set<ScmRepository> set = new LinkedHashSet<>();
        CacheRepositoryKey defaultRepo = null;

        for (Cache.Entry<CacheRepositoryKey, ScmRepository> group : cacheStore.getRepositories()) {
            if (group.getKey().groupId.equals(groupId)) {
                if (group.getKey().isGroup()) defaultRepo = group.getKey();
                set.add(group.getValue());
            }
        }
        Map<CacheRepositoryKey, Set<ScmRepository>> map = new LinkedHashMap<>();
        Objects.requireNonNull(defaultRepo);
        map.put(defaultRepo, set);
        return map.entrySet().iterator().next();
    }

}
