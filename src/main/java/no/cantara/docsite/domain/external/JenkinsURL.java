package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.RepositoryDefinition;
import no.ssb.config.DynamicConfiguration;

import java.util.Objects;

public class JenkinsURL extends ExternalURL<RepositoryDefinition> {

    private static final long serialVersionUID = -3316821555454748209L;
    public static final String KEY = "jenkins";
    private final DynamicConfiguration configuration;
    private boolean useDefaultGroupRepoName;

    public JenkinsURL(DynamicConfiguration configuration, RepositoryDefinition repositoryInfo) {
        super(repositoryInfo);
        this.configuration = configuration;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public void useDefaultGroupRepoName() {
        Objects.requireNonNull(internal.defaultGroupRepoName);
        useDefaultGroupRepoName = true;
    }

    @Override
    public String getExternalURL() {
        return String.format("%s/buildStatus/icon?job=%s", configuration.evaluateToString("jenkins.baseUrl"), (useDefaultGroupRepoName ? internal.defaultGroupRepoName : internal.cacheGroupKey.repoName));
    }
}
