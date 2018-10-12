package no.cantara.docsite.model.github.push;

import no.cantara.docsite.util.JsonUtil;

import javax.json.JsonObject;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import java.util.List;

public class PushPageEvent {

    public @JsonbProperty("after") String afterRevision;
    public Repository repository;
    public JsonObject headCommit;
    public List<Commit> commits;

    @Override
    public String toString() {
        return JsonUtil.asString(this);
    }

    public static class Repository {
        public String name;
        public String fullName;

    }

    public static class Commit {
        public String id;
        public List<String> modified;
    }


    @JsonbTransient
    public boolean isPageCommit() {
        return false;
    }

    @JsonbTransient
    public boolean isCodeCommit() {
        return false;
    }

    @JsonbTransient
    public boolean isReleaseTagCommit() {
        return false;
    }

}
