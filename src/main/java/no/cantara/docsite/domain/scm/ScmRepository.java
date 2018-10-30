package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheGroupKey;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.domain.external.ExternalURL;
import no.cantara.docsite.domain.external.GitHubApiContentsURL;
import no.cantara.docsite.domain.external.GitHubApiReadmeURL;
import no.cantara.docsite.domain.external.GitHubHtmlURL;
import no.cantara.docsite.domain.external.GitHubRawRepoURL;
import no.cantara.docsite.domain.external.JenkinsURL;
import no.cantara.docsite.domain.external.ShieldsIOGroupCommitURL;
import no.cantara.docsite.domain.external.ShieldsIOGroupReleaseURL;
import no.cantara.docsite.domain.external.ShieldsIONoReposURL;
import no.cantara.docsite.domain.external.SnykIOTestBadgeURL;
import no.cantara.docsite.domain.external.SnykIOTestURL;
import no.cantara.docsite.util.JsonbFactory;
import no.ssb.config.DynamicConfiguration;

import javax.json.bind.annotation.JsonbTransient;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

// a loaded instance from e.g. github that described the repo

// https://confluence.atlassian.com/bitbucket/manage-webhooks-735643732.html

// TODO refactor GitHub URLs into a group of urls when adding support for Bitbucket, GitLab etc. Or use ExternalURL all over and add formatter to abstract class. Throw UnsupOp for those urls that has concrete external form

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
    public final Map<String, ExternalURL> externalLinks = new LinkedHashMap<>(); // not immutable

    ScmRepository(DynamicConfiguration configuration, CacheRepositoryKey cacheRepositoryKey, String configDisplayName, String configDescription, String id, String description, String defaultGroupRepoName, String htmlRepoURL) {
        this.config = new Config(configDisplayName, configDescription);
        this.cacheRepositoryKey = cacheRepositoryKey;
        this.id = id;
        this.description = description;
        this.defaultGroupRepoName = defaultGroupRepoName;
        this.repoURL = new GitHubHtmlURL(htmlRepoURL);
        this.apiReadmeURL = new GitHubApiReadmeURL(this);
        this.rawRepoURL = new GitHubRawRepoURL(this);
        this.apiContentsURL = new GitHubApiContentsURL(this);
        externalLinks.put(JenkinsURL.KEY, new JenkinsURL(configuration, this));
        externalLinks.put(ShieldsIONoReposURL.KEY, new ShieldsIONoReposURL(""));
        externalLinks.put(ShieldsIOGroupCommitURL.KEY, new ShieldsIOGroupCommitURL(this));
        externalLinks.put(ShieldsIOGroupReleaseURL.KEY, new ShieldsIOGroupReleaseURL(this));
        externalLinks.put(SnykIOTestURL.KEY, new SnykIOTestURL(this));
        externalLinks.put(SnykIOTestBadgeURL.KEY, new SnykIOTestBadgeURL(this));
    }

    public static class Config implements Serializable {
        private static final long serialVersionUID = -1697907617210696141L;

        public final String displayName;
        public final String description;

        public Config(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
    }


    public static ScmRepository of(DynamicConfiguration configuration, CacheRepositoryKey repositoryDefinition, String configDisplayName, String configDescription,String id, String description, String defaultGroupRepo, String htmlRepoURL) {
        return new ScmRepository(configuration, repositoryDefinition, configDisplayName, configDescription, id, description, defaultGroupRepo, htmlRepoURL);
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
                Objects.equals(externalLinks, that.externalLinks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(config, getCacheRepositoryKey(), id, description, defaultGroupRepoName, repoURL, rawRepoURL, apiReadmeURL, apiContentsURL, externalLinks);
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }
}
