package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheGroupKey;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.domain.config.RepoConfig;
import no.cantara.docsite.domain.links.GitHubApiContentsURL;
import no.cantara.docsite.domain.links.GitHubApiReadmeURL;
import no.cantara.docsite.domain.links.GitHubHtmlURL;
import no.cantara.docsite.domain.links.GitHubRawRepoURL;
import no.cantara.docsite.domain.links.JenkinsURL;
import no.cantara.docsite.domain.links.LicenseURL;
import no.cantara.docsite.domain.links.LinkURL;
import no.cantara.docsite.domain.links.ShieldsIOGitHubIssuesURL;
import no.cantara.docsite.domain.links.ShieldsIOGroupCommitURL;
import no.cantara.docsite.domain.links.ShieldsIOGroupReleaseURL;
import no.cantara.docsite.domain.links.ShieldsIOReposURL;
import no.cantara.docsite.domain.links.SnykIOTestBadgeURL;
import no.cantara.docsite.domain.links.SnykIOTestURL;
import no.cantara.docsite.json.JsonbFactory;
import no.ssb.config.DynamicConfiguration;

import javax.json.bind.annotation.JsonbTransient;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

// a loaded instance from e.g. github that described the repo

// https://confluence.atlassian.com/bitbucket/manage-webhooks-735643732.html

// TODO refactor GitHub URLs into a group of urls when adding support for Bitbucket, GitLab etc. Or use LinkURL all over and add formatter to abstract class. Throw UnsupOp for those urls that has concrete external form

/**
 * A repository definition contains information about the repo, the group it belongs to and service urls
 */
public class ScmRepository implements Serializable {

    private static final long serialVersionUID = 4462535017419847061L;

    public final Config config;
    public final CacheRepositoryKey cacheRepositoryKey;
    public final String id;
    public final String description;
    public final String defaultGroupRepoName;
    public final GitHubHtmlURL repoURL;
    public final GitHubRawRepoURL rawRepoURL;
    public final GitHubApiReadmeURL apiReadmeURL;
    public final GitHubApiContentsURL apiContentsURL;
    public final Map<String, LinkURL> externalLinks = new LinkedHashMap<>(); // not immutable
    public final LicenseURL licenseURL;

    ScmRepository(DynamicConfiguration configuration, CacheRepositoryKey cacheRepositoryKey, String configDisplayName, String configDescription, Map<Class<?>, Object> externalServices, String id, String description, String defaultGroupRepoName, String licenseSpdxId, String htmlRepoURL) {
        this.config = new Config(configDisplayName, configDescription, externalServices);
        this.cacheRepositoryKey = cacheRepositoryKey;
        this.id = id;
        this.description = description;
        this.defaultGroupRepoName = defaultGroupRepoName;
        this.repoURL = new GitHubHtmlURL(htmlRepoURL);
        this.apiReadmeURL = new GitHubApiReadmeURL(cacheRepositoryKey.asCacheKey(), this);
        this.rawRepoURL = new GitHubRawRepoURL(this);
        this.apiContentsURL = new GitHubApiContentsURL(cacheRepositoryKey.asCacheKey(), this);
        this.licenseURL = new LicenseURL(this, licenseSpdxId);

        // TODO this should not be done in constructor
        RepoConfig.Jenkins jenkins = (RepoConfig.Jenkins) externalServices.get(RepoConfig.Jenkins.class);
        String jenkinsPrefix = (jenkins != null ? jenkins.jenkinsPrefix : "");
        externalLinks.put(JenkinsURL.KEY, new JenkinsURL(configuration, cacheRepositoryKey.asCacheKey(), this, jenkinsPrefix));

        externalLinks.put(ShieldsIOReposURL.KEY, new ShieldsIOReposURL(""));
        externalLinks.put(ShieldsIOGroupCommitURL.KEY, new ShieldsIOGroupCommitURL(this));
        externalLinks.put(ShieldsIOGroupReleaseURL.KEY, new ShieldsIOGroupReleaseURL(this));
        externalLinks.put(ShieldsIOGitHubIssuesURL.KEY, new ShieldsIOGitHubIssuesURL(this));
        externalLinks.put(SnykIOTestURL.KEY, new SnykIOTestURL(this));
        externalLinks.put(SnykIOTestBadgeURL.KEY, new SnykIOTestBadgeURL(cacheRepositoryKey.asCacheKey(), this));
    }

    public static class Config implements Serializable {
        private static final long serialVersionUID = -1697907617210696141L;

        public final String displayName;
        public final String description;
        private final Map<Class<?>, Object> externalServices;

        Config(String displayName, String description, Map<Class<?>, Object> externalServices) {
            this.displayName = displayName;
            this.description = description;
            this.externalServices = externalServices;
        }

        @JsonbTransient
        public boolean hasService(Class<?> clazz) {
            return externalServices.containsKey(clazz);
        }

        @JsonbTransient
        public <R> R getService(Class<R> clazz) {
            return (R) externalServices.get(clazz);
        }
    }


    public static ScmRepository of(DynamicConfiguration configuration, CacheRepositoryKey repositoryDefinition, String configDisplayName,
                                   String configDescription, Map<Class<?>, Object> externalServices,
                                   String id, String description, String defaultGroupRepo, String licenseSpdxId, String htmlRepoURL)
    {
        return new ScmRepository(configuration, repositoryDefinition, configDisplayName, configDescription,
                (externalServices == null ? new LinkedHashMap<>() : externalServices),
                id, description, defaultGroupRepo, licenseSpdxId, htmlRepoURL);
    }

    @JsonbTransient
    public CacheGroupKey getCacheGroupKey() {
        return CacheGroupKey.of(cacheRepositoryKey.organization, cacheRepositoryKey.groupId);
    }

    @JsonbTransient
    public CacheRepositoryKey getCacheRepositoryKey() {
        return cacheRepositoryKey;
    }

    @JsonbTransient
    public String getGroupId() {
        return cacheRepositoryKey.groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScmRepository)) return false;
        ScmRepository that = (ScmRepository) o;
        return Objects.equals(config, that.config) &&
                Objects.equals(getCacheRepositoryKey(), that.getCacheRepositoryKey()) &&
                Objects.equals(id, that.id) &&
                Objects.equals(description, that.description) &&
                Objects.equals(defaultGroupRepoName, that.defaultGroupRepoName) &&
                Objects.equals(repoURL, that.repoURL) &&
                Objects.equals(rawRepoURL, that.rawRepoURL) &&
                Objects.equals(apiReadmeURL, that.apiReadmeURL) &&
                Objects.equals(apiContentsURL, that.apiContentsURL) &&
                Objects.equals(externalLinks, that.externalLinks) &&
                Objects.equals(licenseURL, that.licenseURL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(config, getCacheRepositoryKey(), id, description, defaultGroupRepoName, repoURL, rawRepoURL, apiReadmeURL, apiContentsURL, externalLinks, licenseURL);
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }
}
