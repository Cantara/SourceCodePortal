package no.cantara.docsite.domain.config;

import no.cantara.docsite.json.JsonbFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbTypeAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class RepositoryConfig {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryConfig.class);

    public final String title;
    public final Map<ScmProvider, List<Repository>> repositories;
    public final List<RepositoryOverride> repositoryOverrides;
    public final List<Group> groups;

    public RepositoryConfig(String title, Map<ScmProvider, List<Repository>> repositories, List<RepositoryOverride> repositoryOverrides, List<Group> groups) {
        this.title = title;
        this.repositories = Collections.unmodifiableMap(repositories);
        this.repositoryOverrides = Collections.unmodifiableList(repositoryOverrides);
        this.groups = Collections.unmodifiableList(groups);
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

        public static boolean isValid(String name) {
            if (name == null) return false;
            for(ScmProvider value : values()) {
                if (value.provider.equals(name)) {
                    return true;
                }
            }
            return false;
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
        public final String organization;
        public final @JsonbTypeAdapter(JsonbPatternAdapter2.class) Pattern repositoryPattern;
        public final String branch;

        public Repository(String organization, String repositoryPattern, String branch) {
            this.organization = organization;
            this.repositoryPattern = compilePattern(this, repositoryPattern);
            this.branch = branch;
        }

        @Override
        public String toString() {
            return JsonbFactory.asString(this);
        }
    }

    static Pattern compilePattern(Object obj, String pattern) {
        try {
            return Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            throw new RuntimeException("Unable to compile " + obj.getClass().getSimpleName() + " pattern: '" + pattern + "' near " + obj);
        }
    }

    public static class RepositoryOverride {
        public final String repositoryId;
        public final ScmProvider provider;
        public final String organization;
        public final @JsonbTypeAdapter(JsonbPatternAdapter2.class) Pattern repositoryPattern;
        public final String branch;
        public final String displayName;
        public final String description;
        private final Map<Class<?>, ExternalService<?>> services;

        public RepositoryOverride(String repositoryId, String repository, String branch, String displayName, String description,  Map<Class<?>, ExternalService<?>> externalServices) {
            this.repositoryId = repositoryId;
            if (repository == null || !repository.contains(":") || !repository.contains("/")) {
                throw new RuntimeException("The repository must be format: 'github:Organization/RepositoryPattern' <- " + repository);
            }
            String[] matchGroup1 = repository.split(":");
            String[] matchGroup2 = matchGroup1[1].split("/");
            this.provider = ScmProvider.valueOf(matchGroup1[0].toUpperCase());
            this.organization = matchGroup2[0];
            this.branch = branch;
            this.displayName = displayName;
            this.description = description;
            this.services = Collections.unmodifiableMap(externalServices);
            this.repositoryPattern = compilePattern(this, matchGroup2[1]);
        }

        // TODO should be hidden but rendered by json result
        public Map<String, Object> getServices() {
            return services.entrySet().stream().collect(Collectors.toUnmodifiableMap(entry -> entry.getKey().getSimpleName().toLowerCase(), entry -> entry.getValue()));
        }

        @JsonbTransient
        public Map<Class<?>, ExternalService<?>> getExternalServices() {
            return services;
        }

        @JsonbTransient
        public <R> R getService(Class<R> clazz) {
            return (R) services.get(clazz);
        }

        @Override
        public String toString() {
            return JsonbFactory.asString(this);
        }
    }

    public static class Group {
        public final String groupId;
        public final String displayName;
        public final String description;
        public final String defaultEntryRepository;
        public final List<RepositorySelector> repositorySelectors;

        public Group(String groupId, String displayName, String description, String defaultEntryRepository, List<RepositorySelector> repositorySelectors) {
            this.groupId = groupId;
            this.displayName = displayName;
            this.description = description;
            this.defaultEntryRepository = defaultEntryRepository;
            this.repositorySelectors = Collections.unmodifiableList(repositorySelectors);
        }

        @Override
        public String toString() {
            return JsonbFactory.asString(this);
        }
    }

    public static class RepositorySelector {
        public final ScmProvider provider;
        public final String organization;
        public final @JsonbTypeAdapter(JsonbPatternAdapter2.class) Pattern repositorySelector;

        public RepositorySelector(String repositorySelector) {
            if (repositorySelector == null || !repositorySelector.contains(":") || !repositorySelector.contains("/")) {
                throw new RuntimeException("The repositorySelector must be format: 'github:Organization/RepositoryPattern' <- " + repositorySelector);
            }
            String[] matchGroup1 = repositorySelector.split(":");
            String[] matchGroup2 = matchGroup1[1].split("/");
            this.provider = ScmProvider.valueOf(matchGroup1[0].toUpperCase());
            this.organization = matchGroup2[0];
            this.repositorySelector = compilePattern(this, matchGroup2[1]); // should lookup and match with 'repositories'
        }

        @Override
        public String toString() {
            return JsonbFactory.asString(this);
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
        private final Map<ScmProvider, List<RepositoryBuilder>> repositoryBuilderMap = new LinkedHashMap<>();
        private final List<RepositoryOverrideBuilder> repositoryOverrideBuilderList = new ArrayList<>();
        private final List<GroupBuilder> groupBuilderList = new ArrayList<>();

        public RepositoryConfigBuilder(String title) {
            this.title = title;
        }

        public RepositoryConfigBuilder withProvider(RepositoryBuilder repositoryBuilder) {
            if (!repositoryBuilderMap.containsKey(repositoryBuilder.provider)) {
                repositoryBuilder.parent = this;
                List<RepositoryBuilder> repositoryBuilderList = new ArrayList<>();
                repositoryBuilderList.add(repositoryBuilder);
                repositoryBuilderMap.put(repositoryBuilder.provider, repositoryBuilderList);
            } else {
                repositoryBuilderMap.get(repositoryBuilder.provider).add(repositoryBuilder);
            }
            return this;
        }

        public RepositoryConfigBuilder withRepositoryOverride(RepositoryOverrideBuilder repositoryOverrideBuilder) {
            repositoryOverrideBuilderList.add(repositoryOverrideBuilder);
            return this;
        }

        public RepositoryConfigBuilder withGroup(GroupBuilder groupBuilder) {
            groupBuilderList.add(groupBuilder);
            return this;
        }

        public RepositoryConfig build() {
            Map<ScmProvider, List<Repository>> allRepositories = new LinkedHashMap<>();
            for(Map.Entry<ScmProvider, List<RepositoryBuilder>> repository : repositoryBuilderMap.entrySet()) {
                List<RepositoryBuilder> repositoryBuilderList = repository.getValue();
                for (RepositoryBuilder repositoryBuilder : repositoryBuilderList) {
                    Map<ScmProvider, List<Repository>> repositoryMap = repositoryBuilder.build();
                    if (allRepositories.containsKey(repository.getKey())) {
                        List<Repository> repositoryList = allRepositories.get(repository.getKey());
                        repositoryList.addAll(repositoryMap.get(repository.getKey()));

                    } else {
                        allRepositories.putAll(repositoryMap);
                    }
                }
            }

            List<RepositoryOverride> repositoryOverrideList = repositoryOverrideBuilderList.stream().map(m -> m.build()).collect(Collectors.toList());

            List<Group> groupList = groupBuilderList.stream().map(m -> m.build()).collect(Collectors.toList());

            return new RepositoryConfig(title, allRepositories, repositoryOverrideList, groupList);
        }
    }

    public static class RepositoryBuilder {
        private RepositoryConfigBuilder parent;
        final ScmProvider provider;
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

        public Map<ScmProvider, List<Repository>> build() {
            Map<ScmProvider, List<Repository>> repositoriesMap = new LinkedHashMap<>();
            for (MatchRepositoryBuilder matchRepositoryBuilder : matchRepositoryBuilderList) {
                Repository repository = new Repository(repositoryBuilderProps.get("organization"),
                        matchRepositoryBuilder.matchRepositoryBuilderProps.get("repository-pattern"),
                        matchRepositoryBuilder.matchRepositoryBuilderProps.get("branch"));
                if (!repositoriesMap.containsKey(provider)) {
                    List<Repository> repositoryList = new ArrayList<>();
                    repositoryList.add(repository);
                    repositoriesMap.put(provider, repositoryList);
                } else {
                    List<Repository> repositoryList = repositoriesMap.get(provider);
                    repositoryList.add(repository);
                }
            }
            return repositoriesMap;
        }
    }

    public static class MatchRepositoryBuilder {
        private RepositoryBuilder parent;
        final Map<String,String> matchRepositoryBuilderProps = new LinkedHashMap<>();

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
        final Map<String,String> overrideBuilderProps = new LinkedHashMap<>();
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

        public RepositoryOverride build() {
            Map<Class<?>, ExternalService<?>> externalServices = new LinkedHashMap<>();
            for (ExternalBuilder<?> externalBuilder : externalBuilders.externalBuilderProps.values()) {
                ExternalService<?> build = externalBuilder.build();
                externalServices.put(build.getClass(), build);
            }
            return new RepositoryOverride(
                    overrideBuilderProps.get("repository-id"),
                    overrideBuilderProps.get("repository"),
                    overrideBuilderProps.get("branch"),
                    overrideBuilderProps.get("display-name"),
                    overrideBuilderProps.get("description"),
                    externalServices
            );
        }
    }

    // --------------------------------------------------------------------------------------------
    // Group Builders
    // --------------------------------------------------------------------------------------------

    public static GroupBuilder newGroupBuilder() {
        return new GroupBuilder();
    }

    public static class GroupBuilder {
        final Map<String, String> groupBuilderProps = new LinkedHashMap<>();
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

        public GroupBuilder repositorySelector(String repositorySelector) {
            return repositorySelectorBuilder.repositorySelector(repositorySelector);
        }

        public Group build() {
            List<RepositorySelector> repositorySelectorList = new ArrayList<>();
            for(String repositorySelector : repositorySelectorBuilder.repositorySelectorBuilderList) {
                repositorySelectorList.add(new RepositorySelector(repositorySelector));
            }
            return new Group(
                    groupBuilderProps.get("group-id"),
                    groupBuilderProps.get("display-name"),
                    groupBuilderProps.get("description"),
                    groupBuilderProps.get("default-entry-repository"),
                    repositorySelectorList
            );
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
