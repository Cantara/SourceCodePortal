package no.cantara.docsite.cache;

import no.cantara.docsite.domain.config.RepositoryConfigBinding;
import no.cantara.docsite.domain.github.releases.GitHubCreatedTagEvent;
import no.cantara.docsite.domain.maven.MavenPOM;
import no.cantara.docsite.domain.scm.ScmCommitRevision;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.cantara.docsite.domain.scm.ScmRepositoryContents;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CacheStore {

    public static final String CACHE_KEYS = "cacheKeys";
    public static final String CACHE_GROUP_KEYS = "cacheGroupKeys";
    public static final String CACHE_REPOSITORY_KEYS = "cacheRepositoryKeys";
    public static final String REPOSITORIES = "repositories";
    public static final String MAVEN_PROJECT = "mavenProject";
    public static final String CONTENTS = "contents";
    public static final String COMMIT = "commit";
    public static final String RELEASE = "release";
    public static final String CANTARA_WIKI = "cantaraWiki";
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
        if (cacheManager.getCache(CACHE_KEYS) == null) {
            LOG.info("Creating CacheKeys cache");
            MutableConfiguration<CacheKey, CacheRepositoryKey> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(CACHE_KEYS, cacheConfig);
        }

        if (cacheManager.getCache(CACHE_GROUP_KEYS) == null) {
            LOG.info("Creating CacheGroupKeys cache");
            MutableConfiguration<CacheGroupKey, String> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(CACHE_GROUP_KEYS, cacheConfig);
        }

        if (cacheManager.getCache(CACHE_REPOSITORY_KEYS) == null) {
            LOG.info("Creating CacheRepositoryKeys cache");
            MutableConfiguration<CacheRepositoryKey, CacheKey> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(CACHE_REPOSITORY_KEYS, cacheConfig);
        }

        if (cacheManager.getCache(REPOSITORIES) == null) {
            LOG.info("Creating Repositories cache");
            MutableConfiguration<CacheRepositoryKey, ScmRepository> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(REPOSITORIES, cacheConfig);
        }

        if (cacheManager.getCache(MAVEN_PROJECT) == null) {
            LOG.info("Creating Project cache");
            MutableConfiguration<CacheKey, MavenPOM> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(MAVEN_PROJECT, cacheConfig);
        }

        if (cacheManager.getCache(CONTENTS) == null) {
            LOG.info("Creating Page cache");
            MutableConfiguration<CacheKey, ScmRepositoryContents> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(CONTENTS, cacheConfig);
        }

        if (cacheManager.getCache(COMMIT) == null) {
            LOG.info("Creating Commit cache");
            MutableConfiguration<CacheShaKey, ScmCommitRevision> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(COMMIT, cacheConfig);
        }

        if (cacheManager.getCache(RELEASE) == null) {
            LOG.info("Creating Release cache");
            MutableConfiguration<CacheKey, GitHubCreatedTagEvent> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(RELEASE, cacheConfig);
        }

        if (cacheManager.getCache(CANTARA_WIKI) == null) {
            LOG.info("Creating Cantara Wiki cache");
            MutableConfiguration<CacheCantaraWikiKey, String> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache(CANTARA_WIKI, cacheConfig);
        }
    }

    // TODO requires refactoring (prepared done)
    public List<ScmRepository> getRepositoryGroupsByGroupId(String groupId) {
        List<ScmRepository> repositories = new ArrayList<>();
        getRepositories().forEach(a -> {
            if (a.getKey().compareTo(groupId)) {
                repositories.add(a.getValue());
            }
        });
        return repositories;
    }

    public String getConfiguredRepositories() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        for (RepositoryConfigBinding.Repo repo : getGroups()) {
            JsonArrayBuilder groupBuilder = Json.createArrayBuilder();
            List<ScmRepository> repositories = getRepositoryGroupsByGroupId(repo.groupId);
            for (ScmRepository repository : repositories) {
                groupBuilder.add(repository.cacheRepositoryKey.asIdentifier());
            }
            builder.add(repo.groupId, groupBuilder);
        }
        return JsonbFactory.instance().toJson(builder.build());
    }


    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public RepositoryConfigBinding getRepositoryConfig() {
        return repositoryConfig;
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
        return cacheManager.getCache(CACHE_KEYS);
    }

    public Cache<CacheGroupKey, String> getCacheGroupKeys() {
        return cacheManager.getCache(CACHE_GROUP_KEYS);
    }

    // OK
    public Cache<CacheRepositoryKey, CacheKey> getCacheRepositoryKeys() {
        return cacheManager.getCache(CACHE_REPOSITORY_KEYS);
    }

    // OK
    // returns the all matched group keys
    // used by FetchGitHubCommitRevision(s)
    public Set<CacheRepositoryKey> getCacheRepositoryKeys(CacheKey cacheKey) {
        return StreamSupport.stream(getCacheRepositoryKeys().spliterator(), true)
                .filter(entry -> entry.getValue().equals(cacheKey))
                .map(Cache.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // NEW
    public Cache<CacheRepositoryKey, ScmRepository> getRepositories() {
        return cacheManager.getCache(REPOSITORIES);
    }

    // OK
    public Cache<CacheKey, MavenPOM> getMavenProjects() {
        return cacheManager.getCache(MAVEN_PROJECT);
    }

    // OK
    public Cache<CacheKey, ScmRepositoryContents> getReadmeContents() {
        return cacheManager.getCache(CONTENTS);
    }

    // OK
    public Cache<CacheShaKey, ScmCommitRevision> getCommits() {
        return cacheManager.getCache(COMMIT);
    }

    // TBD
    public Cache<CacheKey, GitHubCreatedTagEvent> getReleases() {
        return cacheManager.getCache(RELEASE);
    }

    public Cache<CacheCantaraWikiKey, String> getCantaraWiki() {
        return cacheManager.getCache(CANTARA_WIKI);
    }

}
