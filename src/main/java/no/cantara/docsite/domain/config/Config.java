package no.cantara.docsite.domain.config;

import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class Config {

    public enum Provider {
        GITHUB("github");

        private final String provider;

        Provider(String provider) {
            this.provider = provider;
        }

        public String provider() {
            return provider;
        }
    }

    public final String title;
    public final Deque<Scm> scm = new LinkedList<>();

    public Config(String title) {
        this.title = title;
    }

    public static Builder newBuilder(String title) {
        return new Builder(title);
    }

    public static class Builder {
        private final String title;
        private Map<String,String> props = new LinkedHashMap<>();

        Builder repo(String repoPattern) {
            props.put("repoPattern", repoPattern);
            return this;
        }

        Builder branch(String branchPattern) {
            props.put("branchPattern", branchPattern);
            return this;
        }

        Builder groupId(String groupId) {
            props.put("groupId", groupId);
            return this;
        }

        Builder displayName(String displayName) {
            props.put("displayName", displayName);
            return this;
        }

        Builder description(String description) {
            props.put("description", description);
            return this;
        }

        Builder defaultGroupRepo(String defaultGroupRepo) {
            props.put("defaultGroupRepo", defaultGroupRepo);
            return this;
        }

        public Builder(String title) {
            this.title = title;
        }

        public Config build() {
            return new Config(title);
        }

        public Scm withScm(Provider github) {
//            return new Scm(github);
            return null;
        }
    }

    public static class Group {
        private Map<String,String> props = new LinkedHashMap<>();

        Group organization(String organization) {
            props.put("organization", organization);
            return this;
        }


    }

    public static class Scm {
        public final String organization;
        public final String repoPattern;
        public final String branchPattern;
        public final String groupId;
        public final String displayName;
        public final String description;
        public final String defaultGroupRepo;

        public Scm(String organization, String repoPattern, String branchPattern, String groupId, String displayName, String description, String defaultGroupRepo) {
            this.organization = organization;
            this.repoPattern = repoPattern;
            this.branchPattern = branchPattern;
            this.groupId = groupId;
            this.displayName = displayName;
            this.description = description;
            this.defaultGroupRepo = defaultGroupRepo;
        }
    }
}
