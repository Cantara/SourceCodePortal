package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.ScmRepository;

import java.util.Objects;

public class ShieldsIOGroupCommitURL extends ExternalURL<ScmRepository> {

    private static final long serialVersionUID = 201847051522984036L;
    public static final String KEY = "shieldsGroupCommit";

    public ShieldsIOGroupCommitURL(ScmRepository internal) {
        super(internal);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getExternalURL() {
        return String.format("https://img.shields.io/github/last-commit/%s/%s.svg", internal.cacheRepositoryKey.organization, internal.cacheRepositoryKey.repoName);
    }

    public String getExternalGroupURL() {
        Objects.requireNonNull(internal.defaultGroupRepoName);
        return String.format("https://img.shields.io/github/last-commit/%s/%s.svg", internal.cacheRepositoryKey.organization, internal.defaultGroupRepoName);
    }
}
