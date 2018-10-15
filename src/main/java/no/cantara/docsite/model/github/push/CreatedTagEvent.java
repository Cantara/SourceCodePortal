package no.cantara.docsite.model.github.push;

import no.cantara.docsite.util.JsonUtil;

import javax.json.bind.annotation.JsonbProperty;

public class CreatedTagEvent {

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
        return JsonUtil.asString(this);
    }

    public static class Repository {
        public String name;
    }
}
