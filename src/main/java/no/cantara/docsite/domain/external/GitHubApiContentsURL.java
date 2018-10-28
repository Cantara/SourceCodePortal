package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.RepositoryDefinition;

import java.util.Objects;

public class GitHubApiContentsURL extends ExternalURL<RepositoryDefinition> {

    private static final long serialVersionUID = 6542826512042618912L;
    public static final String KEY = "gitHubApiContentsURL";
    private boolean useDefaultGroupRepoName;

    public GitHubApiContentsURL(RepositoryDefinition repositoryInfo) {
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
        return String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", internal.cacheGroupKey.organization,
                (useDefaultGroupRepoName ? internal.defaultGroupRepoName : internal.cacheGroupKey.repoName), "%s", "%s");
    }

    public String getExternalURL(String relativeFilePath, String commitId) {
        return String.format(getExternalURL(), relativeFilePath, commitId);
    }
    
}
