package no.cantara.docsite.domain.links;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.domain.scm.ScmRepository;

import javax.json.bind.annotation.JsonbTransient;
import java.util.Objects;

public class GitHubApiContentsURL extends LinkURL<CacheKey> {

    private static final long serialVersionUID = 6542826512042618912L;
    public static final String KEY = "gitHubApiContentsURL";
    private final ScmRepository repository;

    public GitHubApiContentsURL(CacheKey cacheKey) {
        this(cacheKey, null);
    }

    public GitHubApiContentsURL(CacheKey cacheKey, ScmRepository repository) {
        super(cacheKey);
        this.repository = repository;
    }

    @Override
    public String getKey() {
        return KEY;
    }
    
    @Override
    public String getExternalURL() {
        return String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", internal.organization, internal.repoName, "%s", "%s");
    }

    public String getExternalURL(String relativeFilePath, String commitId) {
        return String.format(getExternalURL(), relativeFilePath, commitId);
    }

    public String getExternalGroupURL() {
        Objects.requireNonNull(repository);
        Objects.requireNonNull(repository.cacheRepositoryKey);
        return String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", repository.cacheRepositoryKey.organization, repository.defaultGroupRepoName, "%s", "%s");
    }

    @JsonbTransient
    public String getExternalGroupURL(String relativeFilePath, String commitId) {
        return String.format(getExternalGroupURL(), relativeFilePath, commitId);
    }

}
