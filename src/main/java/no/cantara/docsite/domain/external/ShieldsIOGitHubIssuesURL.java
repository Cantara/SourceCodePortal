package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.RepositoryDefinition;

import java.util.Objects;

public class ShieldsIOGitHubIssuesURL extends ExternalURL<RepositoryDefinition> {

    private static final long serialVersionUID = 6817528366457367770L;
    public static final String KEY = "shieldsIOGitHubIssuesURL";
    private boolean useDefaultGroupRepoName;

    public ShieldsIOGitHubIssuesURL(RepositoryDefinition internal) {
        super(internal);
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
        return String.format("https://img.shields.io/github/issues/%s/%s.svg", internal.cacheKey.organization, (useDefaultGroupRepoName ? internal.defaultGroupRepoName : internal.cacheKey.repoName));
    }
}
