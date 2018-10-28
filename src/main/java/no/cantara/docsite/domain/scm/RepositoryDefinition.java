package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheKey;
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

// a loaded instance from e.g. github that described the repo

// https://confluence.atlassian.com/bitbucket/manage-webhooks-735643732.html

// TODO refactor GitHub URLs into a group of urls when adding support for Bitbucket, GitLab etc. Or use ExternalURL all over and add formatter to abstract class. Throw UnsupOp for those urls that has concrete external form

public class RepositoryDefinition {

    public final CacheKey cacheKey;
    public final String id;
    public final String description;
    public final String defaultGroupRepoName;
    public final GitHubHtmlURL repoURL;
    public final GitHubRawRepoURL rawRepoURL;
    public final GitHubApiReadmeURL apiReadmeURL;
    public final GitHubApiContentsURL apiContentsURL;
    public final Map<String, ExternalURL> externalLinks = new LinkedHashMap<>(); // not immutable

    public RepositoryDefinition(CacheKey cacheKey, String id, String description, String defaultGroupRepoName, String htmlRepoURL) {
        this.cacheKey = cacheKey;
        this.id = id;
        this.description = description;
        this.defaultGroupRepoName = defaultGroupRepoName;
        this.repoURL = new GitHubHtmlURL(htmlRepoURL);
        this.rawRepoURL = new GitHubRawRepoURL(this);
        this.apiReadmeURL = new GitHubApiReadmeURL(this);
        this.apiContentsURL = new GitHubApiContentsURL(this);
        externalLinks.put(JenkinsURL.KEY, new JenkinsURL(this));
        externalLinks.put(ShieldsIONoReposURL.KEY, new ShieldsIONoReposURL(""));
        externalLinks.put(ShieldsIOGroupCommitURL.KEY, new ShieldsIOGroupCommitURL(this));
        externalLinks.put(ShieldsIOGroupReleaseURL.KEY, new ShieldsIOGroupReleaseURL(this));
        externalLinks.put(SnykIOTestURL.KEY, new SnykIOTestURL(this));
        externalLinks.put(SnykIOTestBadgeURL.KEY, new SnykIOTestBadgeURL(this));
    }

    public static RepositoryDefinition of(CacheKey cacheKey, String id, String name, String defaultGroupRepo, String description, String htmlRepoURL) {
        return new RepositoryDefinition(cacheKey, id, description, defaultGroupRepo, htmlRepoURL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RepositoryDefinition)) return false;
        RepositoryDefinition that = (RepositoryDefinition) o;
        return Objects.equals(cacheKey, that.cacheKey) &&
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
        return Objects.hash(cacheKey, id, description, defaultGroupRepoName, repoURL, rawRepoURL, apiReadmeURL, apiContentsURL, externalLinks);
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }

}
