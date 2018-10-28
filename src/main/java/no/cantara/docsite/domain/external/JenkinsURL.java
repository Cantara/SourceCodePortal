package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.RepositoryDefinition;
import no.ssb.config.DynamicConfiguration;

import java.util.Objects;

public class JenkinsURL extends ExternalURL<RepositoryDefinition> {

    private static final long serialVersionUID = -3316821555454748209L;
    public static final String KEY = "jenkins";
    private final String baseURL;

    public JenkinsURL(DynamicConfiguration configuration, RepositoryDefinition repositoryInfo) {
        super(repositoryInfo);
        this.baseURL = configuration.evaluateToString("jenkins.baseUrl");
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getExternalURL() {
        return String.format("%s/buildStatus/icon?job=%s", baseURL, internal.cacheGroupKey.repoName);
    }

    public String getExternalGroupURL() {
        Objects.requireNonNull(internal.defaultGroupRepoName);
        return String.format("%s/buildStatus/icon?job=%s", baseURL, internal.defaultGroupRepoName);
    }
}
