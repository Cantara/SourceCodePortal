package no.cantara.docsite.cache;

import no.cantara.docsite.model.github.pull.RepositoryContents;
import no.cantara.docsite.model.github.push.CommitRevision;
import no.cantara.docsite.model.github.push.CreatedTagEvent;
import no.cantara.docsite.model.maven.MavenPOM;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;

public class CacheStore {

    static final Logger LOG = LoggerFactory.getLogger(CacheStore.class);

    private DynamicConfiguration configuration;
    final CacheManager cacheManager;
    Cache<String, MavenPOM> projects;
    Cache<String, RepositoryContents> pages;
    Cache<String, CommitRevision> commits;
    Cache<String, CreatedTagEvent> releases;

    CacheStore(DynamicConfiguration configuration, CacheManager cacheManager) {
        this.configuration = configuration;
        this.cacheManager = cacheManager;
    }

    void initialize() {
        if (cacheManager.getCache("project") == null) {
            LOG.info("Creating Project cache");
            MutableConfiguration<String, MavenPOM> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            projects = cacheManager.createCache("project", cacheConfig);
        }

        if (cacheManager.getCache("page") == null) {
            LOG.info("Creating Page cache");
            MutableConfiguration<String, RepositoryContents> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            pages = cacheManager.createCache("page", cacheConfig);
        }

        if (cacheManager.getCache("commit") == null) {
            LOG.info("Creating Commit cache");
            MutableConfiguration<String, CommitRevision> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            commits = cacheManager.createCache("commit", cacheConfig);
        }

        if (cacheManager.getCache("release") == null) {
            LOG.info("Creating Release cache");
            MutableConfiguration<String, CreatedTagEvent> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            releases = cacheManager.createCache("release", cacheConfig);
        }
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public Cache<String, MavenPOM> getProjects() {
        return projects;
    }

    public Cache<String, RepositoryContents> getPages() {
        return pages;
    }

    public Cache<String, CommitRevision> getCommits() {
        return commits;
    }

    public Cache<String, CreatedTagEvent> getReleases() {
        return releases;
    }
}
