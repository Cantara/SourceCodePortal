package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.RepositoryDefinition;

import java.util.Objects;

public class GitHubRawRepoURL extends ExternalURL<RepositoryDefinition> {

    private static final long serialVersionUID = -5712528210551460515L;
    public static final String KEY = "gitHubRawRepoURL";
    private boolean useDefaultGroupRepoName;

    public GitHubRawRepoURL(RepositoryDefinition repositoryInfo) {
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
        return String.format("https://raw.githubusercontent.com/%s/%s/%s/", internal.cacheGroupKey.organization,
                (useDefaultGroupRepoName ? internal.defaultGroupRepoName : internal.cacheGroupKey.repoName), internal.cacheGroupKey.branch);
    }

}
