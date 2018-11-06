package no.cantara.docsite.domain.links;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.domain.scm.ScmRepository;

import java.util.Objects;

public class SnykIOTestBadgeURL extends LinkURL<CacheKey> {

    private static final long serialVersionUID = 8890035117418785973L;
    public static final String KEY = "snykIOTestBadgeURL";
    private final CacheKey cacheKey;
    private final ScmRepository repository;

    public SnykIOTestBadgeURL(CacheKey cacheKey) {
        this(cacheKey, null);
    }

    public SnykIOTestBadgeURL(CacheKey cacheKey, ScmRepository repository) {
        super(cacheKey);
        this.cacheKey = cacheKey;
        this.repository = repository;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public String getInternalURL() {
        return String.format("/badge/snyk/%s/%s", internal.repoName, internal.branch);
    }

    public String getInternalGroupURL() {
        Objects.requireNonNull(repository);
        Objects.requireNonNull(repository.defaultGroupRepoName);
        return String.format("/badge/snyk/%s/%s", repository.defaultGroupRepoName, repository.cacheRepositoryKey.branch);
    }

    @Override
    public String getExternalURL() {
        return String.format("https://snyk.io/test/github/%s/%s/badge.svg", cacheKey.organization, cacheKey.repoName);
    }

    public String getExternalGroupURL() {
        Objects.requireNonNull(repository);
        Objects.requireNonNull(repository.defaultGroupRepoName);
        return String.format("https://snyk.io/test/github/%s/%s/badge.svg", repository.cacheRepositoryKey.organization, repository.defaultGroupRepoName);
    }
}
