package no.cantara.docsite.domain.github.releases;

import no.cantara.docsite.json.JsonbFactory;

import javax.json.bind.annotation.JsonbProperty;

public class GitHubCreatedTagEvent {

    public String ref;
    public @JsonbProperty("ref_type") String refType;
    public @JsonbProperty("master_branch") String masterBranch;
    public Repository repository;

    public boolean isBranchMaster() {
        return "master".equals(refType);
    }

    public boolean isBranchTag() {
        return "tag".equals(refType);
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }

    public static class Repository {
        public String name;
    }
}
