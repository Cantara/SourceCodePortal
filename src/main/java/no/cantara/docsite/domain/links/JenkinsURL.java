package no.cantara.docsite.domain.links;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.ssb.config.DynamicConfiguration;

import javax.json.bind.annotation.JsonbTransient;
import java.util.Objects;

public class JenkinsURL extends LinkURL<CacheKey> {

    private static final long serialVersionUID = -3316821555454748209L;
    public static final String KEY = "jenkins";
    private final String baseURL;
    private final ScmRepository repository;
    private final String jobPrefix;

    public JenkinsURL(DynamicConfiguration configuration, CacheKey cacheKey, String jobPrefix) {
        this(configuration, cacheKey, null, jobPrefix);
    }

    public JenkinsURL(DynamicConfiguration configuration, CacheKey cacheKey, ScmRepository repository, String jobPrefix) {
        super(cacheKey);
        this.baseURL = configuration.evaluateToString("jenkins.baseUrl");
        this.repository = repository;
        this.jobPrefix = (jobPrefix != null ? jobPrefix : "");
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public String getInternalURL() {
        return String.format("/badge/jenkins/%s/%s", internal.repoName, internal.branch);
    }

    @JsonbTransient
    public String getInternalGroupURL() {
        Objects.requireNonNull(repository);
        Objects.requireNonNull(repository.defaultGroupRepoName);
        return String.format("/badge/jenkins/%s/%s", repository.defaultGroupRepoName, repository.cacheRepositoryKey.branch);
    }

    @Override
    public String getExternalURL() {
        return String.format("%s/buildStatus/icon?job=%s%s", baseURL, jobPrefix, internal.repoName);
    }

    @JsonbTransient
    public String getExternalGroupURL() {
        Objects.requireNonNull(repository);
        Objects.requireNonNull(repository.defaultGroupRepoName);
        return String.format("%s/buildStatus/icon?job=%s%s", baseURL, jobPrefix, repository.defaultGroupRepoName);
    }
}
