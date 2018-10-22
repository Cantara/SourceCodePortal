package no.cantara.docsite.domain.config;

import no.cantara.docsite.util.JsonUtil;

import javax.json.bind.annotation.JsonbProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RepositoryConfig {

    public String title;
    public @JsonbProperty("github") GitHub gitHub;

    @Override
    public String toString() {
        return JsonUtil.asString(this);
    }

    public static class GitHub {
        public String organization;
        public List<Repo> repos = new ArrayList<>();
    }

    public static class Repo {
        public @JsonbProperty("groupId") String groupId;
        public @JsonbProperty("display-name") String displayName;
        public String description;
        public String repo;
        public String branch;
        public @JsonbProperty("default-repo") String defaultRepo; // the project that contains main-repo documentation

        @Override
        public int hashCode() {
            return Objects.hash(groupId, displayName, description, repo, branch);
        }
    }

}
