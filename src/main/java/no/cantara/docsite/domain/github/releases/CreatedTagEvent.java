package no.cantara.docsite.domain.github.releases;

import no.cantara.docsite.util.JsonUtil;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

public class CreatedTagEvent implements Serializable {

    private static final long serialVersionUID = 6663626423947864453L;

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

    public static class Repository implements Serializable {
        private static final long serialVersionUID = 5915280836603198738L;

        public String name;
    }
}
