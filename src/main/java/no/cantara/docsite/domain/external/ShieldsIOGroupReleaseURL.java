package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.RepositoryDefinition;

import java.util.Objects;

public class ShieldsIOGroupReleaseURL extends ExternalURL<RepositoryDefinition> {

    private static final long serialVersionUID = 5485906572024332485L;
    public static final String KEY = "shieldsGroupRelease";
    private boolean useDefaultGroupRepoName;

    public ShieldsIOGroupReleaseURL(RepositoryDefinition internal) {
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
        return String.format("https://img.shields.io/github/tag//%s/%s.svg", internal.cacheKey.organization, (useDefaultGroupRepoName ? internal.defaultGroupRepoName : internal.cacheKey.repoName));
    }
}
