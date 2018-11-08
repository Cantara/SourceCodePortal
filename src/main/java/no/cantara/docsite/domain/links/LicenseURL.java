package no.cantara.docsite.domain.links;

import no.cantara.docsite.domain.scm.ScmRepository;

public class LicenseURL extends LinkURL<ScmRepository> {

    private static final long serialVersionUID = 8561026802157335435L;
    private final String licenseSpdxId;

    public LicenseURL(ScmRepository repository, String licenseSpdxId) {
        super(repository);
        this.licenseSpdxId = licenseSpdxId;
    }

    @Override
    public String getKey() {
        return "license";
    }

    public String getLicenseSpdxId() {
        return licenseSpdxId == null || (licenseSpdxId != null && "NOASSERTION".equalsIgnoreCase(licenseSpdxId)) ? null : licenseSpdxId;
    }

    public String getInternalURL() {
        return String.format("/badge/license/%s/%s", internal.cacheRepositoryKey.repoName, internal.cacheRepositoryKey.branch);
    }

    @Override
    public String getExternalURL() {
        return null;
    }
}
