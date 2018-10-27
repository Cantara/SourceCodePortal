package no.cantara.docsite.domain.config;

import no.cantara.docsite.util.JsonbFactory;

import javax.json.bind.annotation.JsonbProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RepositoryConfigBinding {

    public String title;
    public @JsonbProperty("github") GitHub gitHub;

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }

    public static class GitHub {
        public String organization;
        public Badges badges;
        public List<Repo> repos = new ArrayList<>();
    }

    public static class Badges {
        public String jenkins;
        public @JsonbProperty("snyk.io") String snykIO;
    }

    public static class Repo {
        public @JsonbProperty("groupId") String groupId;
        public @JsonbProperty("display-name") String displayName;
        public String description;
        public String repo;
        public String branch;
        public @JsonbProperty("default-group-repo") String defaultGroupRepo; // the project that contains main-repo documentation

        @Override
        public int hashCode() {
            return Objects.hash(groupId, displayName, description, repo, branch);
        }
    }
}
