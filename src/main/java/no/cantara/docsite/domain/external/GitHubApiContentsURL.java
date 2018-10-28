package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.RepositoryDefinition;

import java.util.Objects;

public class GitHubApiContentsURL extends ExternalURL<RepositoryDefinition> {

    private static final long serialVersionUID = 6542826512042618912L;
    public static final String KEY = "gitHubApiContentsURL";

    public GitHubApiContentsURL(RepositoryDefinition repositoryInfo) {
        super(repositoryInfo);
    }

    @Override
    public String getKey() {
        return KEY;
    }
    
    @Override
    public String getExternalURL() {
        return String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", internal.cacheGroupKey.organization, internal.cacheGroupKey.repoName, "%s", "%s");
    }

    public String getExternalURL(String relativeFilePath, String commitId) {
        return String.format(getExternalURL(), relativeFilePath, commitId);
    }
    
    public String getExternalGroupURL() {
        Objects.requireNonNull(internal.defaultGroupRepoName);
        return String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", internal.cacheGroupKey.organization, internal.defaultGroupRepoName, "%s", "%s");
    }

    public String getExternalGroupURL(String relativeFilePath, String commitId) {
        return String.format(getExternalGroupURL(), relativeFilePath, commitId);
    }

}
