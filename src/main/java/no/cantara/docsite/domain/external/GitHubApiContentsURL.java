package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.ScmGroupRepository;

import javax.json.bind.annotation.JsonbTransient;
import java.util.Objects;

public class GitHubApiContentsURL extends ExternalURL<ScmGroupRepository> {

    private static final long serialVersionUID = 6542826512042618912L;
    public static final String KEY = "gitHubApiContentsURL";

    public GitHubApiContentsURL(ScmGroupRepository repositoryInfo) {
        super(repositoryInfo);
    }

    @Override
    public String getKey() {
        return KEY;
    }
    
    @Override
    public String getExternalURL() {
        return String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", internal.cacheRepositoryKey.organization, internal.cacheRepositoryKey.repoName, "%s", "%s");
    }

    @JsonbTransient
    public String getExternalURL(String relativeFilePath, String commitId) {
        return String.format(getExternalURL(), relativeFilePath, commitId);
    }

    public String getExternalGroupURL() {
        Objects.requireNonNull(internal.defaultGroupRepoName);
        return String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", internal.cacheRepositoryKey.organization, internal.defaultGroupRepoName, "%s", "%s");
    }

    @JsonbTransient
    public String getExternalGroupURL(String relativeFilePath, String commitId) {
        return String.format(getExternalGroupURL(), relativeFilePath, commitId);
    }

}
