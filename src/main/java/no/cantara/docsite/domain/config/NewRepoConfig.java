package no.cantara.docsite.domain.config;

import no.cantara.docsite.json.JsonbFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NewRepoConfig {

    public final String title;
//    public final Map<ScmProvider, List<Repo>> repos;

    public NewRepoConfig(String title, Map<ScmProvider, List<Repository>> repos) {
        this.title = title;
//        this.repos = repos;
    }

    public static Builder newBuilder(String title) {
        return new Builder(title);
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

    public static ScmRepositoryBuilder newScmBuilder(ScmProvider provider) {
        return new ScmRepositoryBuilder(provider);
    }

    public static ScmRepositoryOverrideBuilder newScmRepositoryOverrideBuilder() {
        return new ScmRepositoryOverrideBuilder();
    }

    public static MatchRepositoryBuilder newMatchRepositoryBuilder() {
        return new MatchRepositoryBuilder();
    }

    public static class Builder {
        private final String title;
        private final Map<ScmProvider, ScmRepositoryBuilder> configBuilderProps = new LinkedHashMap<>();
        private final List<ScmRepositoryOverrideBuilder> repositoryOverrideBuilderProps = new ArrayList<>();

        public Builder(String title) {
            this.title = title;
        }

        public Builder withProvider(ScmRepositoryBuilder scmRepositoryBuilder) {
            if (!configBuilderProps.containsKey(scmRepositoryBuilder.provider)) {
                scmRepositoryBuilder.parent = this;
                configBuilderProps.put(scmRepositoryBuilder.provider, scmRepositoryBuilder);
            }
            return this;
        }

        public Builder withRepositoryOverride(ScmRepositoryOverrideBuilder scmRepositoryOverrideBuilder) {
            repositoryOverrideBuilderProps.add(scmRepositoryOverrideBuilder);
            return this;
        }

        public NewRepoConfig build() {
            return null;
        }
    }

    public static class ScmRepositoryBuilder {
        private Builder parent;
        private final ScmProvider provider;
        private final Map<String,String> scmBuilderProps = new LinkedHashMap<>();
        private final List<MatchRepositoryBuilder> scmMatchRepositoryBuilderBuilderProps = new ArrayList<>();

        public ScmRepositoryBuilder(ScmProvider provider) {
            this.provider = provider;
        }

        public ScmRepositoryBuilder organization(String organization) {
            scmBuilderProps.put("organization", organization);
            return this;
        }

        public ScmRepositoryBuilder matchRepository(MatchRepositoryBuilder matchRepositoryBuilder) {
            matchRepositoryBuilder.parent = this;
            scmMatchRepositoryBuilderBuilderProps.add(matchRepositoryBuilder);
            return this;
        }
    }

    public static class MatchRepositoryBuilder {
        private ScmRepositoryBuilder parent;
        private final Map<String,String> scmMatchRepositoryBuilderProps = new LinkedHashMap<>();

        public MatchRepositoryBuilder() {
        }

        public MatchRepositoryBuilder repositoryPattern(String repositoryPattern) {
            scmMatchRepositoryBuilderProps.put("repository-pattern", repositoryPattern);
            return this;
        }

        public MatchRepositoryBuilder branch(String branch) {
            scmMatchRepositoryBuilderProps.put("branch", branch);
            return this;
        }
    }

    public static class ScmRepositoryOverrideBuilder {
        private Builder parent;
        private final Map<String,String> scmOverrideBuilderProps = new LinkedHashMap<>();
        private final ExternalBuilders externalBuilders = new ExternalBuilders();

        public ScmRepositoryOverrideBuilder() {
        }

        public ScmRepositoryOverrideBuilder repositoryId(String repositoryId) {
            scmOverrideBuilderProps.put("repository-id", repositoryId);
            return this;
        }

        public ScmRepositoryOverrideBuilder displayName(String displayName) {
            scmOverrideBuilderProps.put("display-name", displayName);
            return this;
        }

        public ScmRepositoryOverrideBuilder description(String description) {
            scmOverrideBuilderProps.put("description", description);
            return this;
        }

        public ScmRepositoryOverrideBuilder repository(String repository) {
            scmOverrideBuilderProps.put("repository", repository);
            return this;
        }

        public ScmRepositoryOverrideBuilder branch(String branch) {
            scmOverrideBuilderProps.put("branch", branch);
            return this;
        }

        ScmRepositoryOverrideBuilder withExternal(ExternalBuilder<?> externalBuilder) {
            externalBuilders.externalBuilderProps.put(externalBuilder.getConfigKey(), externalBuilder);
            return this;
        }

        ExternalBuilder<?> withExternal(String configKey) {
            if (externalBuilders.externalBuilderProps.containsKey(configKey)) {
                return externalBuilders.externalBuilderProps.get(configKey);
            }

            ExternalBuilder<Object> externalBuilder = ExternalBuilders.getExternalBuilder(configKey);
            externalBuilders.externalBuilderProps.put(externalBuilder.getConfigKey(), externalBuilder);
            return externalBuilder;
        }
    }

    // --------------------------------------------------------------------------------------------
    // External Builders
    // --------------------------------------------------------------------------------------------

    public static JenkinsBuilder newJenkinsBuilder() {
        return new JenkinsBuilder();
    }

    public static SnykBuilder newSnykBuilder() {
        return new SnykBuilder();
    }

    public interface ExternalBuilder<T> {
        static <T> ExternalBuilder<T> create(Class<? extends ExternalBuilder<T>> clazz) {
            try {
                return clazz.getDeclaredConstructor(null).newInstance(null);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        String getConfigKey(); // the key that is found in the repo config.json

        ExternalBuilder<T> set(String key, String value);

        T build();
    }

    public static class ExternalBuilders {
        private static final Map<String, Class<? extends ExternalBuilder<?>>> EXTERNAL_BUILDERS = new LinkedHashMap<>();

        static {
            ExternalBuilders.EXTERNAL_BUILDERS.put("jenkins", JenkinsBuilder.class);
            ExternalBuilders.EXTERNAL_BUILDERS.put("snyk", SnykBuilder.class);
        }

        private Map<String, ExternalBuilder<?>> externalBuilderProps = new LinkedHashMap<>();

        public static <R> ExternalBuilder<R> getExternalBuilder(String configKey) {
            Class<? extends ExternalBuilder<R>> externalBuilderClass = (Class<? extends ExternalBuilder<R>>) EXTERNAL_BUILDERS.get(configKey);
            return ExternalBuilder.create(externalBuilderClass);
        }
    }

    public static class Jenkins implements Serializable {
        private static final long serialVersionUID = 5882777423871329549L;

        public final String jenkinsPrefix;

        public Jenkins(String jenkinsPrefix) {
            this.jenkinsPrefix = jenkinsPrefix;
        }
    }

    public static class JenkinsBuilder implements ExternalBuilder<Jenkins> {
        private Map<String, String> jenkinsBuilderProps = new LinkedHashMap<>();

        @Override
        public String getConfigKey() {
            return "jenkins";
        }

        @Override
        public ExternalBuilder<Jenkins> set(String key, String value) {
            jenkinsBuilderProps.put(key, value);
            return this;
        }


        public JenkinsBuilder prefix(String jenkinsPrefix) {
            jenkinsBuilderProps.put("jenkins-job-prefix", jenkinsPrefix);
            return this;
        }

        @Override
        public Jenkins build() {
            return new Jenkins(jenkinsBuilderProps.get("jenkins-job-prefix"));
        }
    }

    public static class Snyk implements Serializable {
        private static final long serialVersionUID = 846214117299226186L;

        public final String snykTestPrefix;

        public Snyk(String snykTestPrefix) {
            this.snykTestPrefix = snykTestPrefix;
        }
    }

    public static class SnykBuilder implements ExternalBuilder<Snyk> {
        private Map<String, String> snykBuilderProps = new LinkedHashMap<>();

        @Override
        public String getConfigKey() {
            return "snyk";
        }

        @Override
        public ExternalBuilder<Snyk> set(String key, String value) {
            snykBuilderProps.put(key, value);
            return this;
        }

        public SnykBuilder prefix(String snykTestPrefix) {
            snykBuilderProps.put("snyk-test-prefix", snykTestPrefix);
            return this;
        }

        @Override
        public Snyk build() {
            return new Snyk(snykBuilderProps.get("snyk-test-prefix"));
        }
    }

    // --------------------------------------------------------------------------------------------
    // Group Builders
    // --------------------------------------------------------------------------------------------

    public static class GroupBuilder {
        private Map<String, String> groupBuilderProps = new LinkedHashMap<>();

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

        // TODO add new list for repository-selector
    }

}
