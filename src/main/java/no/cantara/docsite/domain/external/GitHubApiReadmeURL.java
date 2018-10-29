package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.ScmGroupRepository;

import java.util.Objects;

public class GitHubApiReadmeURL extends ExternalURL<ScmGroupRepository> {

    private static final long serialVersionUID = -4310813562270437275L;
    public static final String KEY = "gitHubApiReadmeURL";

    public GitHubApiReadmeURL(ScmGroupRepository repositoryInfo) {
        super(repositoryInfo);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getExternalURL() {
        return String.format("https://api.github.com/repos/%s/%s/readme?ref=%s", internal.cacheRepositoryKey.organization, internal.cacheRepositoryKey.repoName, internal.cacheRepositoryKey.branch);
    }

    public String getExternalGroupURL() {
        Objects.requireNonNull(internal.defaultGroupRepoName);
        return String.format("https://api.github.com/repos/%s/%s/readme?ref=%s", internal.cacheRepositoryKey.organization, internal.defaultGroupRepoName, internal.cacheRepositoryKey.branch);
    }

}
