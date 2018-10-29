package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheGroupKey;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.domain.external.ExternalURL;
import no.cantara.docsite.domain.external.GitHubApiContentsURL;
import no.cantara.docsite.domain.external.GitHubRawRepoURL;
import no.cantara.docsite.util.JsonbFactory;
import no.ssb.config.DynamicConfiguration;

import javax.json.bind.annotation.JsonbTransient;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

// a loaded instance from e.g. github that described the repo

// https://confluence.atlassian.com/bitbucket/manage-webhooks-735643732.html

// TODO refactor GitHub URLs into a group of urls when adding support for Bitbucket, GitLab etc. Or use ExternalURL all over and add formatter to abstract class. Throw UnsupOp for those urls that has concrete external form

/**
 * A repository definition contains information about the repo, the group it belongs to and service urls
 */
public class ScmRepository extends ScmGroupRepository {

    private static final long serialVersionUID = 4462535017419847061L;

    public final GitHubRawRepoURL rawRepoURL;
    public final GitHubApiContentsURL apiContentsURL;
    public final Map<String, ExternalURL> externalLinks = new LinkedHashMap<>(); // not immutable

    ScmRepository(DynamicConfiguration configuration, CacheRepositoryKey cacheRepositoryKey, String id, String description, String defaultGroupRepoName, String htmlRepoURL) {
        super(configuration, cacheRepositoryKey, id, description, defaultGroupRepoName, htmlRepoURL, -1);
        this.rawRepoURL = new GitHubRawRepoURL(this);
        this.apiContentsURL = new GitHubApiContentsURL(this);
    }

    public static ScmRepository of(DynamicConfiguration configuration, CacheRepositoryKey repositoryDefinition, String id, String description, String defaultGroupRepo, String htmlRepoURL) {
        return new ScmRepository(configuration, repositoryDefinition, id, description, defaultGroupRepo, htmlRepoURL);
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
        if (!super.equals(o)) return false;
        ScmRepository that = (ScmRepository) o;
        return Objects.equals(rawRepoURL, that.rawRepoURL) &&
                Objects.equals(apiContentsURL, that.apiContentsURL) &&
                Objects.equals(externalLinks, that.externalLinks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rawRepoURL, apiContentsURL, externalLinks);
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }
}
