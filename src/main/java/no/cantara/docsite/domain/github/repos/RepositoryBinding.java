package no.cantara.docsite.domain.github.repos;

import no.cantara.docsite.util.JsonbFactory;

import javax.json.bind.annotation.JsonbProperty;
import java.util.Date;

public class RepositoryBinding {

    public String id;
    public String name;
    public String description;
    public @JsonbProperty("full_name") String fullName;
    public @JsonbProperty("private") boolean visible;
    public String language;
    public License license;
    public @JsonbProperty("created_at") Date created;
    public @JsonbProperty("updated_at") Date updated;
    public @JsonbProperty("pushed_at") Date pushed;
    public @JsonbProperty("default_branch") String defaultBranch;
    public @JsonbProperty("html_url") String htmlUrl;
    public @JsonbProperty("git_url") String gitUrl;
    public @JsonbProperty("ssh_url") String sshUrl;
    public @JsonbProperty("git_refs_url") String gitRefsUrl;
    public @JsonbProperty("git_tags_url") String gitTagsUrl;
    public @JsonbProperty("commits_url") String commitsUrl;
    public Owner owner;

    public static class Owner {
        public String id;
        public String login;
        public String url;
        public String type;
        public @JsonbProperty("repos_url") String reposUrl;
        public @JsonbProperty("avatar_url") String avatarUrl;
    }

    public static class License {
        public String key;
        public String name;
        public @JsonbProperty("spdx_id") String spdxId;
        public String url;
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }
}
