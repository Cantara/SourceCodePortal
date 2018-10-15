package no.cantara.docsite.cache;

import no.cantara.docsite.model.github.pull.RepositoryContents;
import no.cantara.docsite.model.github.push.CommitRevision;
import no.cantara.docsite.model.github.push.CreatedTagEvent;
import no.cantara.docsite.model.maven.MavenPOM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;

public class CacheStore {

    static final Logger LOG = LoggerFactory.getLogger(CacheStore.class);

    final CacheManager cacheManager;
    Cache<String, MavenPOM> projects;
    Cache<String, RepositoryContents> pages;
    Cache<String, CommitRevision> commits;
    Cache<String, CreatedTagEvent> releases;

    CacheStore(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    void initialize() {
        if (cacheManager.getCache("project") == null) {
            LOG.info("Creating Project cache");
            projects = cacheManager.createCache("project", new MutableConfiguration<>());
        }

        if (cacheManager.getCache("page") == null) {
            LOG.info("Creating Page cache");
            pages = cacheManager.createCache("page", new MutableConfiguration<>());
        }

        if (cacheManager.getCache("commit") == null) {
            LOG.info("Creating Commit cache");
            commits = cacheManager.createCache("commit", new MutableConfiguration<>());
        }

        if (cacheManager.getCache("release") == null) {
            LOG.info("Creating Release cache");
            releases = cacheManager.createCache("release", new MutableConfiguration<>());
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
