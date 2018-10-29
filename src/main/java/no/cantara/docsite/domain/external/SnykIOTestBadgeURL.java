package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.ScmGroupRepository;

import java.util.Objects;

public class SnykIOTestBadgeURL extends ExternalURL<ScmGroupRepository> {

    private static final long serialVersionUID = 8890035117418785973L;
    public static final String KEY = "snykIOTestBadgeURL";

    public SnykIOTestBadgeURL(ScmGroupRepository internal) {
        super(internal);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getExternalURL() {
        return String.format("https://snyk.io/test/github/%s/%s/badge.svg", internal.cacheRepositoryKey.organization, internal.cacheRepositoryKey.repoName);
    }

    public String getExternalGroupURL() {
        Objects.requireNonNull(internal.defaultGroupRepoName);
        return String.format("https://snyk.io/test/github/%s/%s/badge.svg", internal.cacheRepositoryKey.organization, internal.defaultGroupRepoName);
    }
}
