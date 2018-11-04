package no.cantara.docsite.domain.links;

import no.cantara.docsite.domain.scm.ScmRepository;

import java.util.Objects;

public class SnykIOTestBadgeURL extends LinkURL<ScmRepository> {

    private static final long serialVersionUID = 8890035117418785973L;
    public static final String KEY = "snykIOTestBadgeURL";

    public SnykIOTestBadgeURL(ScmRepository repository) {
        super(repository);
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
