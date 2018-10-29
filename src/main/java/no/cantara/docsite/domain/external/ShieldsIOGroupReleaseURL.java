package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.ScmGroupRepository;

import java.util.Objects;

public class ShieldsIOGroupReleaseURL extends ExternalURL<ScmGroupRepository> {

    private static final long serialVersionUID = 5485906572024332485L;
    public static final String KEY = "shieldsGroupRelease";

    public ShieldsIOGroupReleaseURL(ScmGroupRepository internal) {
        super(internal);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getExternalURL() {
        return String.format("https://img.shields.io/github/tag//%s/%s.svg", internal.cacheRepositoryKey.organization, internal.cacheRepositoryKey.repoName);
    }

    public String getExternalGroupURL() {
        Objects.requireNonNull(internal.defaultGroupRepoName);
        return String.format("https://img.shields.io/github/tag//%s/%s.svg", internal.cacheRepositoryKey.organization, internal.defaultGroupRepoName);
    }
}
