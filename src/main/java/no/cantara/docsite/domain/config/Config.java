package no.cantara.docsite.domain.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class Config {

    public enum ScmProvider {
        GITHUB("github"),
        BITBUCKET("bitbucket");

        private final String provider;

        ScmProvider(String provider) {
            this.provider = provider;
        }

        public String provider() {
            return provider;
        }
    }

    public final String title;

    public Config(String title) {
        this.title = title;
    }


    public static Builder newBuilder(String title) {
        return new Builder(title);
    }

    public static ScmBuilder newScmGroupBuilder(String groupId) {
        return new ScmBuilder(groupId);
    }

    public static class Builder {
        private final String title;
        private final Map<ScmProvider, ScmGroupBuilder> builderMap = new LinkedHashMap<>();

        public Builder(String title) {
            this.title = title;
        }

        public Config build() {
            return new Config(title);
        }

        public Builder withProvider(ScmProvider provider) {
            if (!builderMap.containsKey(provider)) builderMap.put(provider, new ScmGroupBuilder(this));
            return this;
        }

        // add repo
        public Builder withGroup(ScmGroup group) {
//            if (!builderMap.containsKey(group.groupId)) builderMap.put(group.groupId, group);
//            return new ScmGroup(null, null, null, null, null, null, null);
            return null;
        }
    }

    public static class ScmBuilder {
        private final Map<String, ScmGroup> props = new LinkedHashMap<>();
        private final String groupId;

        public ScmBuilder(String groupId) {
            this.groupId = groupId;
        }

        public ScmGroup build() {
            return null;
        }

    }

    public static class ScmGroup {
//        public final String organization;
//        public final String repoPattern;
//        public final String branchPattern;
//        public final String groupId;
//        public final String displayName;
//        public final String description;
//        public final String defaultGroupRepo;
//        private final Builder parent;

        public ScmGroup(String organization, String repoPattern, String branchPattern, String groupId, String displayName, String description, String defaultGroupRepo) {
//            this.organization = organization;
//            this.repoPattern = repoPattern;
//            this.branchPattern = branchPattern;
//            this.groupId = groupId;
//            this.displayName = displayName;
//            this.description = description;
//            this.defaultGroupRepo = defaultGroupRepo;
        }

        public ScmGroup(Builder parent) {
//            this.parent = parent;
        }

        public Config build() {
            return null;
        }
    }

    public static class ScmGroupBuilder {
        private final Map<String, String> props = new LinkedHashMap<>();
        private final Builder parent;

        public ScmGroupBuilder(Builder parent) {
            this.parent = parent;
        }

        ScmGroupBuilder repo(String repoPattern) {
            props.put("repoPattern", repoPattern);
            return this;
        }

        ScmGroupBuilder branch(String branchPattern) {
            props.put("branchPattern", branchPattern);
            return this;
        }

        ScmGroupBuilder groupId(String groupId) {
            props.put("groupId", groupId);
            return this;
        }

        ScmGroupBuilder displayName(String displayName) {
            props.put("displayName", displayName);
            return this;
        }

        ScmGroupBuilder description(String description) {
            props.put("description", description);
            return this;
        }

        ScmGroupBuilder defaultGroupRepo(String defaultGroupRepo) {
            props.put("defaultGroupRepo", defaultGroupRepo);
            return this;
        }

    }
}
