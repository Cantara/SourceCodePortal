package no.cantara.docsite.domain.config;

import no.cantara.docsite.json.JsonbFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RepositoryConfig {

    public final String title;
//    public final Map<ScmProvider, List<Repo>> repos;

    public RepositoryConfig(String title, Map<ScmProvider, List<Repository>> repos) {
        this.title = title;
//        this.repos = repos;
    }

    public static RepositoryConfigBuilder newBuilder(String title) {
        return new RepositoryConfigBuilder(title);
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
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

    public static class Repository {
        private String organization;

        public Repository(String organization) {
            this.organization = organization;
        }
    }

    // --------------------------------------------------------------------------------------------
    // Builders
    // --------------------------------------------------------------------------------------------

    public static RepositoryBuilder newScmBuilder(ScmProvider provider) {
        return new RepositoryBuilder(provider);
    }

    public static RepositoryOverrideBuilder newScmRepositoryOverrideBuilder() {
        return new RepositoryOverrideBuilder();
    }

    public static MatchRepositoryBuilder newMatchRepositoryBuilder() {
        return new MatchRepositoryBuilder();
    }

    public static class RepositoryConfigBuilder {
        private final String title;
        private final Map<ScmProvider, RepositoryBuilder> repositoryBuilderProps = new LinkedHashMap<>();
        private final List<RepositoryOverrideBuilder> repositoryOverrideBuilderProps = new ArrayList<>();
        private final List<GroupBuilder> groupBuilderProps = new ArrayList<>();

        public RepositoryConfigBuilder(String title) {
            this.title = title;
        }

        public RepositoryConfigBuilder withProvider(RepositoryBuilder repositoryBuilder) {
            if (!repositoryBuilderProps.containsKey(repositoryBuilder.provider)) {
                repositoryBuilder.parent = this;
                repositoryBuilderProps.put(repositoryBuilder.provider, repositoryBuilder);
            }
            return this;
        }

        public RepositoryConfigBuilder withRepositoryOverride(RepositoryOverrideBuilder repositoryOverrideBuilder) {
            repositoryOverrideBuilderProps.add(repositoryOverrideBuilder);
            return this;
        }

        public RepositoryConfigBuilder withGroup(GroupBuilder groupBuilder) {
            groupBuilderProps.add(groupBuilder);
            return this;
        }

        public RepositoryConfig build() {
            RepositoryConfig config = new RepositoryConfig(title, null);

            return null;
        }
    }

    public static class RepositoryBuilder {
        private RepositoryConfigBuilder parent;
        private final ScmProvider provider;
        private final Map<String,String> repositoryBuilderProps = new LinkedHashMap<>();
        private final List<MatchRepositoryBuilder> matchRepositoryBuilderList = new ArrayList<>();

        public RepositoryBuilder(ScmProvider provider) {
            this.provider = provider;
        }

        public RepositoryBuilder organization(String organization) {
            repositoryBuilderProps.put("organization", organization);
            return this;
        }

        public RepositoryBuilder matchRepository(MatchRepositoryBuilder matchRepositoryBuilder) {
            matchRepositoryBuilder.parent = this;
            matchRepositoryBuilderList.add(matchRepositoryBuilder);
            return this;
        }
    }

    public static class MatchRepositoryBuilder {
        private RepositoryBuilder parent;
        private final Map<String,String> matchRepositoryBuilderProps = new LinkedHashMap<>();

        public MatchRepositoryBuilder() {
        }

        public MatchRepositoryBuilder repositoryPattern(String repositoryPattern) {
            matchRepositoryBuilderProps.put("repository-pattern", repositoryPattern);
            return this;
        }

        public MatchRepositoryBuilder branch(String branch) {
            matchRepositoryBuilderProps.put("branch", branch);
            return this;
        }
    }

    public static class RepositoryOverrideBuilder {
        private RepositoryConfigBuilder parent;
        private final Map<String,String> overrideBuilderProps = new LinkedHashMap<>();
        private final ExternalBuilders externalBuilders = new ExternalBuilders();

        public RepositoryOverrideBuilder() {
        }

        public RepositoryOverrideBuilder repositoryId(String repositoryId) {
            overrideBuilderProps.put("repository-id", repositoryId);
            return this;
        }

        public RepositoryOverrideBuilder displayName(String displayName) {
            overrideBuilderProps.put("display-name", displayName);
            return this;
        }

        public RepositoryOverrideBuilder description(String description) {
            overrideBuilderProps.put("description", description);
            return this;
        }

        public RepositoryOverrideBuilder repository(String repository) {
            overrideBuilderProps.put("repository", repository);
            return this;
        }

        public RepositoryOverrideBuilder branch(String branch) {
            overrideBuilderProps.put("branch", branch);
            return this;
        }

        RepositoryOverrideBuilder withExternal(ExternalBuilder<?> externalBuilder) {
            externalBuilders.externalBuilderProps.put(externalBuilder.getConfigKey(), externalBuilder);
            return this;
        }

        @Deprecated
        ExternalBuilder<?> withExternal(String configKey) {
            if (externalBuilders.externalBuilderProps.containsKey(configKey)) {
                return externalBuilders.externalBuilderProps.get(configKey);
            }

            ExternalBuilder<?> externalBuilder = ExternalBuilders.getExternalBuilder(configKey);
            externalBuilders.externalBuilderProps.put(externalBuilder.getConfigKey(), externalBuilder);
            return externalBuilder;
        }
    }

    // --------------------------------------------------------------------------------------------
    // Group Builders
    // --------------------------------------------------------------------------------------------

    public static GroupBuilder newGroupBuilder() {
        return new GroupBuilder();
    }

    public static class GroupBuilder {
        private final Map<String, String> groupBuilderProps = new LinkedHashMap<>();
        private final RepositorySelectorBuilder repositorySelectorBuilder = new RepositorySelectorBuilder(this);

        public GroupBuilder groupId(String groupId) {
            groupBuilderProps.put("group-id", groupId);
            return this;
        }

        public GroupBuilder displayName(String displayName) {
            groupBuilderProps.put("display-name", displayName);
            return this;
        }

        public GroupBuilder description(String description) {
            groupBuilderProps.put("description", description);
            return this;
        }

        public GroupBuilder defaultEntryRepository(String defaultEntryRepository) {
            groupBuilderProps.put("default-entry-repository", defaultEntryRepository);
            return this;
        }

        public GroupBuilder repositorySelector(String repositorSelector) {
            return repositorySelectorBuilder.repositorySelector(repositorSelector);
        }

    }

    public static class RepositorySelectorBuilder {
        private final GroupBuilder parent;
        private final  List<String> repositorySelectorBuilderList = new ArrayList<>();

        RepositorySelectorBuilder(GroupBuilder parent) {
            this.parent = parent;
        }

        public GroupBuilder repositorySelector(String repositorySelector) {
            repositorySelectorBuilderList.add(repositorySelector);
            return parent;
        }
    }

}
