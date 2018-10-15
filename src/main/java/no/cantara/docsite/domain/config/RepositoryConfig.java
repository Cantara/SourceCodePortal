package no.cantara.docsite.domain.config;

import no.cantara.docsite.util.JsonUtil;

import javax.json.bind.annotation.JsonbProperty;
import java.util.ArrayList;
import java.util.List;

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
        public String repo;
        public String branch;
    }

}
