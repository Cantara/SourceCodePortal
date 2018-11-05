package no.cantara.docsite.domain.links;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.domain.scm.ScmRepository;

import java.util.Objects;

public class GitHubApiReadmeURL extends LinkURL<CacheKey> {

    private static final long serialVersionUID = -4310813562270437275L;
    public static final String KEY = "gitHubApiReadmeURL";
    private final ScmRepository repository;

    public GitHubApiReadmeURL(CacheKey cacheKey) {
        this(cacheKey, null);
    }

    public GitHubApiReadmeURL(CacheKey cacheKey, ScmRepository repository) {
        super(cacheKey);
        this.repository = repository;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public String getInternalURL() {
        return String.format("/contents/%s/%s", internal.repoName, internal.branch);
    }

    @Override
    public String getExternalURL() {
        return String.format("https://api.github.com/repos/%s/%s/readme?ref=%s", internal.organization, internal.repoName, internal.branch);
    }

    public String getExternalGroupURL() {
        Objects.requireNonNull(repository);
        Objects.requireNonNull(repository.cacheRepositoryKey);
        return String.format("https://api.github.com/repos/%s/%s/readme?ref=%s", repository.cacheRepositoryKey.organization, repository.defaultGroupRepoName, repository.cacheRepositoryKey.branch);
    }

}
