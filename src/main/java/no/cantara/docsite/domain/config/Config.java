package no.cantara.docsite.domain.config;

import no.cantara.docsite.json.JsonbFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Config {

    public final String title;
    public final Map<ScmProvider,List<Repo>> repos = new LinkedHashMap<>();

    public Config(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Config)) return false;
        Config config = (Config) o;
        return Objects.equals(title, config.title) &&
                Objects.equals(repos, config.repos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, repos);
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }

    public static Builder newBuilder(String title) {
        return new Builder(title);
    }

    public static RepoBuilder newRepoBuilder(String groupId) {
        return new RepoBuilder(groupId);
    }

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

        @Override
        public String toString() {
            return provider;
        }
    }

    public static class Repo {
        public final String organization;
        public final String repoPattern;
        public final String branchPattern;
        public final String groupId;
        public final String displayName;
        public final String description;
        public final String defaultGroupRepo;

        public Repo(String organization, String repoPattern, String branchPattern, String groupId, String displayName, String description, String defaultGroupRepo) {
            this.organization = organization;
            this.repoPattern = repoPattern;
            this.branchPattern = branchPattern;
            this.groupId = groupId;
            this.displayName = displayName;
            this.description = description;
            this.defaultGroupRepo = defaultGroupRepo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Repo)) return false;
            Repo repo = (Repo) o;
            return Objects.equals(organization, repo.organization) &&
                    Objects.equals(repoPattern, repo.repoPattern) &&
                    Objects.equals(branchPattern, repo.branchPattern) &&
                    Objects.equals(groupId, repo.groupId) &&
                    Objects.equals(displayName, repo.displayName) &&
                    Objects.equals(description, repo.description) &&
                    Objects.equals(defaultGroupRepo, repo.defaultGroupRepo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(organization, repoPattern, branchPattern, groupId, displayName, description, defaultGroupRepo);
        }

        @Override
        public String toString() {
            return "Repo{" +
                    "organization='" + organization + '\'' +
                    ", repoPattern='" + repoPattern + '\'' +
                    ", branchPattern='" + branchPattern + '\'' +
                    ", groupId='" + groupId + '\'' +
                    ", displayName='" + displayName + '\'' +
                    ", description='" + description + '\'' +
                    ", defaultGroupRepo='" + defaultGroupRepo + '\'' +
                    '}';
        }
    }

    public static class Builder {
        private final String title;
        private final Map<ScmProvider, GroupBuilder> builderMap = new LinkedHashMap<>();

        public Builder(String title) {
            this.title = title;
        }

        public Config build() {
            Config config = new Config(title);
            for (Map.Entry<ScmProvider, GroupBuilder> entry : builderMap.entrySet()) {
                List<Repo> repoList = new ArrayList<>();
                for (List<RepoBuilder> repoBuilderList : entry.getValue().builderMap.values()) {
                    for(RepoBuilder repoBuilder : repoBuilderList) {
                        repoList.add(repoBuilder.build());
                    }
                }
                config.repos.put(entry.getKey(), repoList);
            }
            return config;
        }

        public GroupBuilder withProvider(ScmProvider provider, String organization) {
            if (!builderMap.containsKey(provider)) builderMap.put(provider, new GroupBuilder(this, organization));
            return builderMap.get(provider);
        }
    }

    public static class GroupBuilder {

        private final Map<String, List<RepoBuilder>> builderMap = new LinkedHashMap<>();
        private final Builder parent;
        private final String organization;

        public GroupBuilder(Builder parent, String organization) {
            this.parent = parent;
            this.organization = organization;
        }

        public GroupBuilder withRepo(RepoBuilder group) {
            if (!builderMap.containsKey(group.props.get("groupId"))) {
                group.parent = this;
                List<RepoBuilder> repoBuilderList = new ArrayList<>();
                repoBuilderList.add(group);
                builderMap.put(group.props.get("groupId"), repoBuilderList);
            } else {
                List<RepoBuilder> repoBuilderList = builderMap.get(group.props.get("groupId"));
                group.parent = this;
                repoBuilderList.add(group);
            }
            return this;
        }

        public Config build() {
            return parent.build();
        }
    }

    public static class RepoBuilder {
        private final Map<String, String> props = new LinkedHashMap<>();
        private GroupBuilder parent;

        public RepoBuilder(String groupId) {
            props.put("groupId", groupId);
        }

        RepoBuilder repo(String repoPattern) {
            props.put("repoPattern", repoPattern);
            return this;
        }

        RepoBuilder branch(String branchPattern) {
            props.put("branchPattern", branchPattern);
            return this;
        }

        RepoBuilder displayName(String displayName) {
            props.put("displayName", displayName);
            return this;
        }

        RepoBuilder description(String description) {
            props.put("description", description);
            return this;
        }

        RepoBuilder defaultGroupRepo(String defaultGroupRepo) {
            props.put("defaultGroupRepo", defaultGroupRepo);
            return this;
        }

        public Builder done() {
            return parent.parent;
        }

        Repo build() {
            return new Repo(parent.organization, props.get("repoPattern"), props.get("branchPattern"), props.get("groupId"), props.get("displayName"), props.get("description"), props.get("defaultGroupRepo"));
        }
    }
}
