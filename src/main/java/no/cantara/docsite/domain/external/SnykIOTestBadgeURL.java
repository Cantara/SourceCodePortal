package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.RepositoryDefinition;

import java.util.Objects;

public class SnykIOTestBadgeURL extends ExternalURL<RepositoryDefinition> {

    private static final long serialVersionUID = 8890035117418785973L;
    public static final String KEY = "snykIOTestBadgeURL";
    private boolean useDefaultGroupRepoName;

    public SnykIOTestBadgeURL(RepositoryDefinition internal) {
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
        return String.format("https://snyk.io/test/github/%s/%s/badge.svg", internal.cacheGroupKey.organization, (useDefaultGroupRepoName ? internal.defaultGroupRepoName : internal.cacheGroupKey.repoName));
    }
}
