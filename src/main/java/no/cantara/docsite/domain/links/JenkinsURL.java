package no.cantara.docsite.domain.links;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.ssb.config.DynamicConfiguration;

import java.util.Objects;

public class JenkinsURL extends LinkURL<CacheKey> {

    private static final long serialVersionUID = -3316821555454748209L;
    public static final String KEY = "jenkins";
    private final String baseURL;
    private final ScmRepository repository;

    public JenkinsURL(DynamicConfiguration configuration, CacheKey cacheKey) {
        this(configuration, cacheKey, null);
    }

    public JenkinsURL(DynamicConfiguration configuration, CacheKey cacheKey, ScmRepository repository) {
        super(cacheKey);
        this.baseURL = configuration.evaluateToString("jenkins.baseUrl");
        this.repository = repository;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getExternalURL() {
        return String.format("%s/buildStatus/icon?job=%s", baseURL, internal.repoName);
    }

    public String getExternalGroupURL() {
        Objects.requireNonNull(repository);
        Objects.requireNonNull(repository.defaultGroupRepoName);
        return String.format("%s/buildStatus/icon?job=%s", baseURL, repository.defaultGroupRepoName);
    }
}
