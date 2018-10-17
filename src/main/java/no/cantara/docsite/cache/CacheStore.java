package no.cantara.docsite.cache;

import no.cantara.docsite.domain.config.Repository;
import no.cantara.docsite.domain.config.RepositoryConfig;
import no.cantara.docsite.domain.github.commits.CommitRevision;
import no.cantara.docsite.domain.github.contents.RepositoryContents;
import no.cantara.docsite.domain.github.releases.CreatedTagEvent;
import no.cantara.docsite.domain.maven.MavenPOM;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyNamingStrategy;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CacheStore {

    static final Logger LOG = LoggerFactory.getLogger(CacheStore.class);

    final DynamicConfiguration configuration;
    final CacheManager cacheManager;
    final RepositoryConfig repositoryConfig;

    CacheStore(DynamicConfiguration configuration, CacheManager cacheManager) {
        this.configuration = configuration;
        this.cacheManager = cacheManager;
        this.repositoryConfig = load();
    }

    RepositoryConfig load() {
        try (InputStream json = ClassLoader.getSystemResourceAsStream("conf/config.json")) {
            JsonbConfig config = new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES);
            Jsonb jsonb = JsonbBuilder.create(config);
            return jsonb.fromJson(json, RepositoryConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void initialize() {
        if (cacheManager.getCache("cacheKeys") == null) {
            LOG.info("Creating Grouped repositories cache");
            MutableConfiguration<CacheKey, CacheGroupKey> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache("cacheKeys", cacheConfig);
        }

        if (cacheManager.getCache("repositoryGroup") == null) {
            LOG.info("Creating Grouped repositories cache");
            MutableConfiguration<CacheGroupKey, Repository> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache("repositoryGroup", cacheConfig);
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
            MutableConfiguration<CacheKey, RepositoryContents> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache("page", cacheConfig);
        }

        if (cacheManager.getCache("commit") == null) {
            LOG.info("Creating Commit cache");
            MutableConfiguration<CacheShaKey, CommitRevision> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache("commit", cacheConfig);
        }

        if (cacheManager.getCache("release") == null) {
            LOG.info("Creating Release cache");
            MutableConfiguration<CacheKey, CreatedTagEvent> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            cacheManager.createCache("release", cacheConfig);
        }
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public RepositoryConfig getRepositoryConfig() {
        return repositoryConfig;
    }

    public List<String> getGroups() {
        List<String> groups = new ArrayList<>();
        repositoryConfig.gitHub.repos.forEach(r -> {
            groups.add(r.groupId);
        });
        return groups;
    }

    public Cache<CacheKey, CacheGroupKey> getCacheKeys() {
        return cacheManager.getCache("cacheKeys");
    }

    public Cache<CacheGroupKey, Repository> getRepositoryGroups() {
        return cacheManager.getCache("repositoryGroup");
    }

    public List<Repository> getRepositoryGroupsByGroupId(String groupId) {
        List<Repository> repositories = new ArrayList<>();
        getRepositoryGroups().forEach(a -> {
            if (a.getKey().compareTo(groupId)) {
                repositories.add(a.getValue());
            }
        });
        return repositories;
    }

    public Cache<CacheKey, MavenPOM> getProjects() {
        return cacheManager.getCache("project");
    }

    public Cache<CacheKey, RepositoryContents> getPages() {
        return cacheManager.getCache("page");
    }

    public Cache<CacheShaKey, CommitRevision> getCommits() {
        return cacheManager.getCache("commit");
    }

    public Cache<CacheKey, CreatedTagEvent> getReleases() {
        return cacheManager.getCache("release");
    }

}
