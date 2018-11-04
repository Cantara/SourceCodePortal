package no.cantara.docsite.domain.links;

import no.cantara.docsite.domain.scm.ScmRepository;

import java.util.Objects;

public class GitHubApiReadmeURL extends LinkURL<ScmRepository> {

    private static final long serialVersionUID = -4310813562270437275L;
    public static final String KEY = "gitHubApiReadmeURL";

    public GitHubApiReadmeURL(ScmRepository repository) {
        super(repository);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public String getInternalURL() {
        return String.format("/contents/%s/%s", internal.cacheRepositoryKey.repoName, internal.cacheRepositoryKey.branch);
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
