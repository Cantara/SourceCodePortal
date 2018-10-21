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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Repo)) return false;
            Repo repo1 = (Repo) o;
            return Objects.equals(groupId, repo1.groupId) &&
                    Objects.equals(displayName, repo1.displayName) &&
                    Objects.equals(description, repo1.description) &&
                    Objects.equals(repo, repo1.repo) &&
                    Objects.equals(branch, repo1.branch);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, displayName, description, repo, branch);
        }
    }

}
