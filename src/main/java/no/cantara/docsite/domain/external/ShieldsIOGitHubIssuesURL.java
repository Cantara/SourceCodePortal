package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.ScmGroupRepository;
import no.cantara.docsite.domain.scm.ScmRepository;

import java.util.Objects;

public class ShieldsIOGitHubIssuesURL extends ExternalURL<ScmGroupRepository> {

    private static final long serialVersionUID = 6817528366457367770L;
    public static final String KEY = "shieldsIOGitHubIssuesURL";

    public ShieldsIOGitHubIssuesURL(ScmRepository internal) {
        super(internal);
    }

    @Override
    public String getKey() {
        return KEY;
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
