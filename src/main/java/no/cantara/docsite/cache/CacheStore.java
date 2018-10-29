package no.cantara.docsite.cache;

import no.cantara.docsite.domain.config.Repository;
import no.cantara.docsite.domain.config.RepositoryConfigBinding;
import no.cantara.docsite.domain.github.releases.GitHubCreatedTagEvent;
import no.cantara.docsite.domain.maven.MavenPOM;
import no.cantara.docsite.domain.scm.ScmCommitRevision;
import no.cantara.docsite.domain.scm.ScmRepositoryContents;
import no.cantara.docsite.domain.scm.ScmRepositoryDefinition;
import no.cantara.docsite.util.JsonbFactory;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CacheStore {

    static final Logger LOG = LoggerFactory.getLogger(CacheStore.class);

    final DynamicConfiguration configuration;
    final CacheManager cacheManager;
    final RepositoryConfigBinding repositoryConfig;

    CacheStore(DynamicConfiguration configuration, CacheManager cacheManager) {
        this.configuration = configuration;
        this.cacheManager = cacheManager;
        this.repositoryConfig = load();
    }

    RepositoryConfigBinding load() {
        try (InputStream json = ClassLoader.getSystemResourceAsStream("conf/config.json")) {
            return JsonbFactory.instance().fromJson(json, RepositoryConfigBinding.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void initialize() {
        if (cacheManager.getCache("cacheKeys") == null) {
            LOG.info("Creating CacheKeys cache");
            MutableConfiguration<CacheKey, CacheRepositoryKey> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache("cacheKeys", cacheConfig);
        }

        if (cacheManager.getCache("cacheGroupKeys") == null) {
            LOG.info("Creating CacheGroupKeys cache");
            MutableConfiguration<CacheGroupKey, String> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache("cacheGroupKeys", cacheConfig);
        }

        if (cacheManager.getCache("cacheRepositoryKeys") == null) {
            LOG.info("Creating CacheRepositoryKeys cache");
            MutableConfiguration<CacheRepositoryKey, CacheKey> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache("cacheRepositoryKeys", cacheConfig);
        }

        if (cacheManager.getCache("repositoryGroup") == null) {
            LOG.info("Creating Grouped repositories cache");
            MutableConfiguration<CacheRepositoryKey, Repository> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache("repositoryGroup", cacheConfig);
        }

        if (cacheManager.getCache("repositories") == null) {
            LOG.info("Creating Repositories cache");
            MutableConfiguration<CacheRepositoryKey, ScmRepositoryDefinition> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache("repositories", cacheConfig);
        }

        if (cacheManager.getCache("project") == null) {
            LOG.info("Creating Project cache");
            MutableConfiguration<CacheKey, MavenPOM> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache("project", cacheConfig);
        }

        if (cacheManager.getCache("page") == null) {
            LOG.info("Creating Page cache");
            MutableConfiguration<CacheKey, ScmRepositoryContents> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache("page", cacheConfig);
        }

        if (cacheManager.getCache("commit") == null) {
            LOG.info("Creating Commit cache");
            MutableConfiguration<CacheShaKey, ScmCommitRevision> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache("commit", cacheConfig);
        }

        if (cacheManager.getCache("release") == null) {
            LOG.info("Creating Release cache");
            MutableConfiguration<CacheKey, GitHubCreatedTagEvent> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache("release", cacheConfig);
        }

        if (cacheManager.getCache("cantaraWiki") == null) {
            LOG.info("Creating Cantara Wiki cache");
            MutableConfiguration<CacheCantaraWikiKey, String> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache("cantaraWiki", cacheConfig);
        }
    }

    public String getConfiguredRepositories() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        for(RepositoryConfigBinding.Repo repo : getGroups()) {
            JsonArrayBuilder groupBuilder = Json.createArrayBuilder();
            List<ScmRepositoryDefinition> repositories = getRepositoryGroupsByGroupId(repo.groupId);
            for(ScmRepositoryDefinition repository : repositories) {
                groupBuilder.add(repository.cacheRepositoryKey.asIdentifier());
            }
            builder.add(repo.groupId, groupBuilder);
        }
        return JsonbFactory.instance().toJson(builder.build());
    }


    public CacheManager getCacheManager() {
        return cacheManager;
    }

    @Deprecated // TODO this should not be leaked out into the app, because it is confusing
    public RepositoryConfigBinding getRepositoryConfig() {
        return repositoryConfig;
    }

    public RepositoryConfigBinding.Repo getGroupByGroupId(String groupId) {
        Objects.requireNonNull(groupId);
        for(RepositoryConfigBinding.Repo repo : repositoryConfig.gitHub.repos) {
            if (groupId.equals(repo.groupId)) {
                return repo;
            }
        }
        return null;
    }

    public List<RepositoryConfigBinding.Repo> getGroups() {
        List<RepositoryConfigBinding.Repo> groups = new ArrayList<>();
        repositoryConfig.gitHub.repos.forEach(r -> {
            groups.add(r);
        });
        return groups;
    }

    // OK
    public Cache<CacheKey, CacheRepositoryKey> getCacheKeys() {
        return cacheManager.getCache("cacheKeys");
    }

    public Cache<CacheGroupKey, String> getCacheGroupKeys() {
        return cacheManager.getCache("cacheGroupKeys");
    }

    // OK
    @Deprecated
    public Cache<CacheRepositoryKey,CacheKey> getCacheRepositoryKeys() {
        return cacheManager.getCache("cacheRepositoryKeys");
    }

    // OK
    // returns the first found group key
    public CacheRepositoryKey getCacheRepositoryKey(CacheKey cacheKey) {
        Set<CacheRepositoryKey> groupKeys = StreamSupport.stream(getCacheRepositoryKeys().spliterator(), true)
                .filter(entry -> entry.getValue().equals(cacheKey))
                .map(Cache.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return groupKeys.stream().filter(entry -> entry.repoName.toLowerCase().contains(entry.groupId.toLowerCase())).findFirst()
                .orElse(groupKeys.iterator().hasNext() ? groupKeys.iterator().next() : null);
    }

    // OK
    // returns the all matched group keys
    public Set<CacheRepositoryKey> getCacheRepositoryKeys(CacheKey cacheKey) {
        return StreamSupport.stream(getCacheRepositoryKeys().spliterator(), true)
                .filter(entry -> entry.getValue().equals(cacheKey))
                .map(Cache.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // TODO requires refactoring (prepared done)
    public Cache<CacheRepositoryKey, Repository> getRepositoryGroups() {
        return cacheManager.getCache("repositoryGroup");
    }

    // TODO requires refactoring (prepared done)
    public List<ScmRepositoryDefinition> getRepositoryGroupsByGroupId(String groupId) {
        List<ScmRepositoryDefinition> repositories = new ArrayList<>();
        getRepositories().forEach(a -> {
            if (a.getKey().compareTo(groupId)) {
                repositories.add(a.getValue());
            }
        });
        return repositories;
    }

    @Deprecated // unused
    public List<Repository> getRepositoryGroupsByName(String name) {
        List<Repository> repositories = new ArrayList<>();
        getRepositoryGroups().forEach(a -> {
            if (name.equals(a.getValue().name)) {
                repositories.add(a.getValue());
            }
        });
        return repositories;
    }

    // NEW
    public Cache<CacheRepositoryKey, ScmRepositoryDefinition> getRepositories() {
        return cacheManager.getCache("repositories");
    }

    // OK
    public Cache<CacheKey, MavenPOM> getProjects() {
        return cacheManager.getCache("project");
    }

    // OK
    public Cache<CacheKey, ScmRepositoryContents> getPages() {
        return cacheManager.getCache("page");
    }

    // OK
    public Cache<CacheShaKey, ScmCommitRevision> getCommits() {
        return cacheManager.getCache("commit");
    }

    // TBD
    public Cache<CacheKey, GitHubCreatedTagEvent> getReleases() {
        return cacheManager.getCache("release");
    }

    public Cache<CacheCantaraWikiKey, String> getCantaraWiki() {
        return cacheManager.getCache("cantaraWiki");
    }

}
