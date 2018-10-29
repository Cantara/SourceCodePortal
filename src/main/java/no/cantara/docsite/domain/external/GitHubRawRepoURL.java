package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.ScmGroupRepository;

import java.util.Objects;

public class GitHubRawRepoURL extends ExternalURL<ScmGroupRepository> {

    private static final long serialVersionUID = -5712528210551460515L;
    public static final String KEY = "gitHubRawRepoURL";

    public GitHubRawRepoURL(ScmGroupRepository repositoryInfo) {
        super(repositoryInfo);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getExternalURL() {
        return String.format("https://raw.githubusercontent.com/%s/%s/%s/", internal.cacheRepositoryKey.organization, internal.cacheRepositoryKey.repoName, internal.cacheRepositoryKey.branch);
    }

    public String getExternalGroupURL() {
        Objects.requireNonNull(internal.defaultGroupRepoName);
        return String.format("https://raw.githubusercontent.com/%s/%s/%s/", internal.cacheRepositoryKey.organization, internal.defaultGroupRepoName, internal.cacheRepositoryKey.branch);
    }

}
