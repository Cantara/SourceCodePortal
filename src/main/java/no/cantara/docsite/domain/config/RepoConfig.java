package no.cantara.docsite.domain.config;

import no.cantara.docsite.json.JsonbFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RepoConfig {

    public final String title;
    public final Map<ScmProvider,List<Repo>> repos = new LinkedHashMap<>();

    public RepoConfig(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RepoConfig)) return false;
        RepoConfig repoConfig = (RepoConfig) o;
        return Objects.equals(title, repoConfig.title) &&
                Objects.equals(repos, repoConfig.repos);
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

    public static RepoBuilder newRepoBuilder() {
        return new RepoBuilder();
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
        private final Map<ScmProvider, GroupBuilder> configBuilderProps = new LinkedHashMap<>();

        public Builder(String title) {
            this.title = title;
        }

        public RepoConfig build() {
            RepoConfig repoConfig = new RepoConfig(title);
            for (Map.Entry<ScmProvider, GroupBuilder> entry : configBuilderProps.entrySet()) {
                //GroupBuilder groupBuilder = configBuilderProps.get(entry.getKey());
                //String organization = groupBuilder.organization;
                List<Repo> repoList = new ArrayList<>();
                for (List<RepoBuilder> repoBuilderList : entry.getValue().groupBuilderProps.values()) {
                    for(RepoBuilder repoBuilder : repoBuilderList) {
                        repoList.add(repoBuilder.build());
                    }
                }
                repoConfig.repos.put(entry.getKey(), repoList);
            }
            return repoConfig;
        }

        public GroupBuilder withProvider(ScmProvider provider, String organization) {
            if (!configBuilderProps.containsKey(provider)) configBuilderProps.put(provider, new GroupBuilder(this, organization));
            return configBuilderProps.get(provider);
        }
    }

    public static class GroupBuilder {

        private final Map<String, List<RepoBuilder>> groupBuilderProps = new LinkedHashMap<>();
        private final Builder parent;
        private final String organization;

        public GroupBuilder(Builder parent, String organization) {
            this.parent = parent;
            this.organization = organization;
        }

        public GroupBuilder withRepo(RepoBuilder group) {
            if (!groupBuilderProps.containsKey(group.repoBulderProps.get("groupId"))) {
                group.parent = this;
                List<RepoBuilder> repoBuilderList = new ArrayList<>();
                repoBuilderList.add(group);
                groupBuilderProps.put(group.repoBulderProps.get("groupId"), repoBuilderList);
            } else {
                List<RepoBuilder> repoBuilderList = groupBuilderProps.get(group.repoBulderProps.get("groupId"));
                group.parent = this;
                repoBuilderList.add(group);
            }
            return this;
        }

        public RepoConfig build() {
            return parent.build();
        }
    }

    public static class RepoBuilder {
        private final Map<String, String> repoBulderProps = new LinkedHashMap<>();
        private GroupBuilder parent;

        public RepoBuilder() {
        }

        RepoBuilder groupId(String groupId) {
            repoBulderProps.put("groupId", groupId);
            return this;
        }

        RepoBuilder repo(String repoPattern) {
            repoBulderProps.put("repoPattern", repoPattern);
            return this;
        }

        RepoBuilder branch(String branchPattern) {
            repoBulderProps.put("branchPattern", branchPattern);
            return this;
        }

        RepoBuilder displayName(String displayName) {
            repoBulderProps.put("displayName", displayName);
            return this;
        }

        RepoBuilder description(String description) {
            repoBulderProps.put("description", description);
            return this;
        }

        RepoBuilder defaultGroupRepo(String defaultGroupRepo) {
            repoBulderProps.put("defaultGroupRepo", defaultGroupRepo);
            return this;
        }

        Repo build() {
            return new Repo(parent.organization, repoBulderProps.get("repoPattern"), repoBulderProps.get("branchPattern"), repoBulderProps.get("groupId"), repoBulderProps.get("displayName"), repoBulderProps.get("description"), repoBulderProps.get("defaultGroupRepo"));
        }
    }
}
