package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.RepositoryDefinition;

import java.util.Objects;

public class ShieldsIOGroupCommitURL extends ExternalURL<RepositoryDefinition> {

    private static final long serialVersionUID = 201847051522984036L;
    public static final String KEY = "shieldsGroupCommit";
    private boolean useDefaultGroupRepoName;

    public ShieldsIOGroupCommitURL(RepositoryDefinition internal) {
        super(internal);
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
        return String.format("https://img.shields.io/github/last-commit/%s/%s.svg", internal.cacheKey.organization, (useDefaultGroupRepoName ? internal.defaultGroupRepoName : internal.cacheKey.repoName));
    }
}
