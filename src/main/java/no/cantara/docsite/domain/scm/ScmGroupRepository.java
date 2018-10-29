package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.domain.external.ExternalURL;
import no.cantara.docsite.domain.external.GitHubApiReadmeURL;
import no.cantara.docsite.domain.external.GitHubHtmlURL;
import no.cantara.docsite.domain.external.JenkinsURL;
import no.cantara.docsite.domain.external.ShieldsIOGroupCommitURL;
import no.cantara.docsite.domain.external.ShieldsIOGroupReleaseURL;
import no.cantara.docsite.domain.external.ShieldsIONoReposURL;
import no.cantara.docsite.domain.external.SnykIOTestBadgeURL;
import no.cantara.docsite.domain.external.SnykIOTestURL;
import no.cantara.docsite.util.JsonbFactory;
import no.ssb.config.DynamicConfiguration;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ScmGroupRepository implements Serializable {

    private static final long serialVersionUID = -2271824222539605362L;

    public final CacheRepositoryKey cacheRepositoryKey;
    public final String id;
    public final String description;
    public final String defaultGroupRepoName;
    public final GitHubHtmlURL repoURL;
    public final GitHubApiReadmeURL apiReadmeURL;
    public final Map<String, ExternalURL> externalLinks = new LinkedHashMap<>(); // not immutable
    public final int numberOfRepos;

    public ScmGroupRepository(DynamicConfiguration configuration, CacheRepositoryKey cacheRepositoryKey, String id, String description, String defaultGroupRepoName, String htmlRepoURL, int numberOfRepos) {
        this.cacheRepositoryKey = cacheRepositoryKey;
        this.id = id;
        this.description = description;
        this.defaultGroupRepoName = defaultGroupRepoName;
        this.repoURL = new GitHubHtmlURL(htmlRepoURL);
        this.apiReadmeURL = new GitHubApiReadmeURL(this);
        this.numberOfRepos = numberOfRepos;
        externalLinks.put(JenkinsURL.KEY, new JenkinsURL(configuration, this));
        externalLinks.put(ShieldsIONoReposURL.KEY, new ShieldsIONoReposURL(""));
        externalLinks.put(ShieldsIOGroupCommitURL.KEY, new ShieldsIOGroupCommitURL(this));
        externalLinks.put(ShieldsIOGroupReleaseURL.KEY, new ShieldsIOGroupReleaseURL(this));
        externalLinks.put(SnykIOTestURL.KEY, new SnykIOTestURL(this));
        externalLinks.put(SnykIOTestBadgeURL.KEY, new SnykIOTestBadgeURL(this));
    }

    public static ScmGroupRepository of(DynamicConfiguration configuration, CacheRepositoryKey cacheRepositoryKey, String id, String description, String defaultGroupRepoName, String htmlRepoURL, int numberOfRepos) {
        return new ScmGroupRepository(configuration, cacheRepositoryKey, id, description, defaultGroupRepoName, htmlRepoURL, numberOfRepos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScmGroupRepository)) return false;
        ScmGroupRepository that = (ScmGroupRepository) o;
        return numberOfRepos == that.numberOfRepos &&
                Objects.equals(cacheRepositoryKey, that.cacheRepositoryKey) &&
                Objects.equals(id, that.id) &&
                Objects.equals(description, that.description) &&
                Objects.equals(defaultGroupRepoName, that.defaultGroupRepoName) &&
                Objects.equals(repoURL, that.repoURL) &&
                Objects.equals(apiReadmeURL, that.apiReadmeURL) &&
                Objects.equals(externalLinks, that.externalLinks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cacheRepositoryKey, id, description, defaultGroupRepoName, repoURL, apiReadmeURL, externalLinks, numberOfRepos);
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }
}
