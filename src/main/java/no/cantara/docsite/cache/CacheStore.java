package no.cantara.docsite.cache;

import no.cantara.docsite.domain.config.Repository;
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
import java.util.concurrent.ConcurrentHashMap;

public class CacheStore {

    static final Logger LOG = LoggerFactory.getLogger(CacheStore.class);

    final DynamicConfiguration configuration;
    final CacheManager cacheManager;

    Cache<CacheGroupKey, Repository> repositoryGroups;
    Cache<CacheKey, MavenPOM> projects;
    Cache<CacheKey, RepositoryContents> pages;
    Cache<CacheShaKey, CommitRevision> commits; // TODO use a revision key
    Cache<CacheKey, ConcurrentHashMap<String,CreatedTagEvent>> releases;

    CacheStore(DynamicConfiguration configuration, CacheManager cacheManager) {
        this.configuration = configuration;
        this.cacheManager = cacheManager;
    }

    void initialize() {
        if (cacheManager.getCache("repostioryGroup") == null) {
            LOG.info("Creating Grouped repositories cache");
            MutableConfiguration<CacheGroupKey, Repository> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            repositoryGroups = cacheManager.createCache("repostioryGroup", cacheConfig);
        }

        if (cacheManager.getCache("project") == null) {
            LOG.info("Creating Project cache");
            MutableConfiguration<CacheKey, MavenPOM> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            projects = cacheManager.createCache("project", cacheConfig);
        }

        if (cacheManager.getCache("page") == null) {
            LOG.info("Creating Page cache");
            MutableConfiguration<CacheKey, RepositoryContents> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            pages = cacheManager.createCache("page", cacheConfig);
        }

        if (cacheManager.getCache("commit") == null) {
            LOG.info("Creating Commit cache");
            MutableConfiguration<CacheShaKey, CommitRevision> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            commits = cacheManager.createCache("commit", cacheConfig);
        }

        if (cacheManager.getCache("release") == null) {
            LOG.info("Creating Release cache");
            MutableConfiguration<CacheKey, ConcurrentHashMap<String,CreatedTagEvent>> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setManagementEnabled(configuration.evaluateToBoolean("cache.management"));
            cacheConfig.setStatisticsEnabled(configuration.evaluateToBoolean("cache.statistics"));
            releases = cacheManager.createCache("release", cacheConfig);
        }
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public Cache<CacheGroupKey, Repository> getRepositoryGroups() {
        return repositoryGroups;
    }

    public Cache<CacheKey, MavenPOM> getProjects() {
        return projects;
    }

    public Cache<CacheKey, RepositoryContents> getPages() {
        return pages;
    }

    public Cache<CacheShaKey, CommitRevision> getCommits() {
        return commits;
    }

    public Cache<CacheKey, ConcurrentHashMap<String,CreatedTagEvent>> getReleases() {
        return releases;
    }
}
