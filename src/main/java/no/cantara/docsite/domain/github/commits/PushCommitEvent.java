package no.cantara.docsite.domain.github.commits;

import no.cantara.docsite.util.JsonUtil;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import java.util.List;

public class PushCommitEvent {

    public String ref;
    public @JsonbProperty("before") String beforeRevision;
    public @JsonbProperty("after") String afterRevision;
    public Repository repository;
    public @JsonbProperty("head_commit") HeadCommit headCommit;
    public List<Commit> commits;

    @JsonbTransient
    public String getBranch() {
        String[] refArray = ref.split("/");
        return refArray[refArray.length-1];
    }

    @Override
    public String toString() {
        return JsonUtil.asString(this);
    }

    public static class Repository {
        public String name;
        public String fullName;
        public Owner owner;
    }

    public static class Owner {
        public String name;
    }

    public static class HeadCommit {
        public String id;
        public @JsonbProperty("tree_id") String treeId;
        public String message;
        public @JsonbProperty("modified") List<String> modifiedList;
    }

    public static class Commit {
        public String id;
        public List<String> modified;
    }


    @JsonbTransient
    public boolean isPageCommit() {
        boolean isValidReadme = false;
        for (String modified : headCommit.modifiedList) {
            if (!headCommit.modifiedList.isEmpty() && (modified.toLowerCase().endsWith(".md") || modified.toLowerCase().endsWith(".ad") || modified.toLowerCase().endsWith(".adoc"))) {
                isValidReadme = true;
                break;
            }
        }
        return isCodeCommit() && isValidReadme;
    }

    @JsonbTransient
    public boolean isCodeCommit() {
        return commits.size() > 0;
    }

    @JsonbTransient
    public boolean isReleaseTagCommit() {
        return false;
    }

}
