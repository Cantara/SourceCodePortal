package no.cantara.docsite.domain.links;

import no.cantara.docsite.domain.scm.ScmRepository;

import javax.json.bind.annotation.JsonbTransient;
import java.util.Objects;

public class ShieldsIOGitHubIssuesURL extends LinkURL<ScmRepository> {

    private static final long serialVersionUID = 6817528366457367770L;
    public static final String KEY = "shieldsIOGitHubIssuesURL";

    public ShieldsIOGitHubIssuesURL(ScmRepository repository) {
        super(repository);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public String getInternalURL() {
        return String.format("/badge/shields-issues/%s/%s", internal.cacheRepositoryKey.repoName, internal.cacheRepositoryKey.branch);
    }

    @JsonbTransient
    public String getInternalGroupURL() {
        Objects.requireNonNull(internal);
        Objects.requireNonNull(internal.defaultGroupRepoName);
        return String.format("/badge/shields-issues/%s/%s", internal.defaultGroupRepoName, internal.cacheRepositoryKey.branch);
    }

    @Override
    public String getExternalURL() {
        return String.format("https://img.shields.io/github/issues/%s/%s.svg", internal.cacheRepositoryKey.organization, internal.cacheRepositoryKey.repoName);
    }

    public String getExternalGroupURL() {
        Objects.requireNonNull(internal.defaultGroupRepoName);
        return String.format("https://img.shields.io/github/issues/%s/%s.svg", internal.cacheRepositoryKey.organization, internal.defaultGroupRepoName);
    }
}
