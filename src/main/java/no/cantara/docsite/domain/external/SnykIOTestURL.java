package no.cantara.docsite.domain.external;

import no.cantara.docsite.domain.scm.ScmGroupRepository;

import java.util.Objects;

public class SnykIOTestURL extends ExternalURL<ScmGroupRepository> {

    private static final long serialVersionUID = 8890035117418785973L;
    public static final String KEY = "snykIOTestURL";

    public SnykIOTestURL(ScmGroupRepository internal) {
        super(internal);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getExternalURL() {
        return String.format("https://snyk.io/test/github/%s/%s", internal.cacheRepositoryKey.organization, internal.cacheRepositoryKey.repoName);
    }

    public String getExternalGroupURL() {
        Objects.requireNonNull(internal.defaultGroupRepoName);
        return String.format("https://snyk.io/test/github/%s/%s", internal.cacheRepositoryKey.organization, internal.defaultGroupRepoName);
    }
}
