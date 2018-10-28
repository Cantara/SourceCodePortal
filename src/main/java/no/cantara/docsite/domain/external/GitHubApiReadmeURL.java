package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.RepositoryDefinition;

import java.util.Objects;

public class GitHubApiReadmeURL extends ExternalURL<RepositoryDefinition> {

    private static final long serialVersionUID = -4310813562270437275L;
    public static final String KEY = "gitHubApiReadmeURL";
    private boolean useDefaultGroupRepoName;

    public GitHubApiReadmeURL(RepositoryDefinition repositoryInfo) {
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
        return String.format("https://api.github.com/repos/%s/%s/readme?ref=%s", internal.cacheGroupKey.organization,
                (useDefaultGroupRepoName ? internal.defaultGroupRepoName : internal.cacheGroupKey.repoName), internal.cacheGroupKey.branch);
    }

}
