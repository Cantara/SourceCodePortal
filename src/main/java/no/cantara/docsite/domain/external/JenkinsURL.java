package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.RepositoryDefinition;

import java.util.Objects;

public class JenkinsURL extends ExternalURL<RepositoryDefinition> {

    private static final long serialVersionUID = -3316821555454748209L;
    public static final String KEY = "jenkins";
    private boolean useDefaultGroupRepoName;

    public JenkinsURL(RepositoryDefinition repositoryInfo) {
        super(repositoryInfo);
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
        return String.format("https://jenkins.capraconsulting.no/buildStatus/icon?job=%s", (useDefaultGroupRepoName ? internal.defaultGroupRepoName : internal.cacheKey.repoName));
    }
}
