package no.cantara.docsite.domain.config;

import no.cantara.docsite.json.JsonbFactory;

import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbTypeAdapter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RepoConfig {

    public final String title;
    public final Map<ScmProvider, List<Repo>> repos;

    public RepoConfig(String title, Map<ScmProvider, List<Repo>> repos) {
        this.title = title;
        this.repos = repos;
    }

    public static Builder newBuilder(String title) {
        return new Builder(title);
    }

    public static RepoBuilder newRepoBuilder() {
        return new RepoBuilder();
    }

    public static JenkinsBuilder newJenkinsBuilder() {
        return new JenkinsBuilder();
    }

    public static SnykBuilder newSnykBuilder() {
        return new SnykBuilder();
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

    public static class Repo {
        public final String organization;
        public final @JsonbTypeAdapter(JsonbPatternAdapter.class) Pattern[] repoPatterns;
        public final String branchPattern;
        public final String groupId;
        public final String displayName;
        public final String description;
        public final String defaultGroupRepo;
        private final Map<Class<?>, Object> services = new LinkedHashMap<>();

        public Repo(String organization, List<String> repoPatterns, String branchPattern, String groupId, String displayName, String description, String defaultGroupRepo) {
            this.organization = organization;
            this.repoPatterns = repoPatterns.stream().map(rp -> Pattern.compile(rp)).collect(Collectors.toList()).toArray(new Pattern[repoPatterns.size()]);
            this.branchPattern = branchPattern;
            this.groupId = groupId;
            this.displayName = displayName;
            this.description = description;
            this.defaultGroupRepo = defaultGroupRepo;
        }


        // TODO should be hidden but rendered by json result
        public Map<String, Object> getServices() {
            return services.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().getSimpleName().toLowerCase(), entry -> entry.getValue()));
        }

        @JsonbTransient
        public Map<Class<?>, Object> getExternalServices() {
            return services;
        }

        @JsonbTransient
        public <R> R getService(Class<R> clazz) {
            return (R) services.get(clazz);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Repo)) return false;
            Repo repo = (Repo) o;
            return Objects.equals(organization, repo.organization) &&
                    Objects.equals(repoPatterns, repo.repoPatterns) &&
                    Objects.equals(branchPattern, repo.branchPattern) &&
                    Objects.equals(groupId, repo.groupId) &&
                    Objects.equals(displayName, repo.displayName) &&
                    Objects.equals(description, repo.description) &&
                    Objects.equals(defaultGroupRepo, repo.defaultGroupRepo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(organization, repoPatterns, branchPattern, groupId, displayName, description, defaultGroupRepo);
        }

        @Override
        public String toString() {
            return "Repo{" +
                    "organization='" + organization + '\'' +
                    ", repoPatterns='" + repoPatterns + '\'' +
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
            Map<ScmProvider, List<Repo>> repos = new LinkedHashMap<>();
            for (Map.Entry<ScmProvider, GroupBuilder> entry : configBuilderProps.entrySet()) {
                //GroupBuilder groupBuilder = configBuilderProps.get(entry.getKey());
                //String organization = groupBuilder.organization;
                List<Repo> repoList = new ArrayList<>();
                for (List<RepoBuilder> repoBuilderList : entry.getValue().groupBuilderProps.values()) {
                    for (RepoBuilder repoBuilder : repoBuilderList) {
                        repoList.add(repoBuilder.build());
                    }
                }
                repos.put(entry.getKey(), Collections.unmodifiableList(repoList));
            }
            return new RepoConfig(title, Collections.unmodifiableMap(repos));
        }

        public GroupBuilder withProvider(ScmProvider provider, String organization) {
            if (!configBuilderProps.containsKey(provider))
                configBuilderProps.put(provider, new GroupBuilder(this, organization));
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
            if (!groupBuilderProps.containsKey(group.repoBuilderProps.get("groupId"))) {
                group.parent = this;
                List<RepoBuilder> repoBuilderList = new ArrayList<>();
                repoBuilderList.add(group);
                groupBuilderProps.put(group.repoBuilderProps.get("groupId"), repoBuilderList);
            } else {
                List<RepoBuilder> repoBuilderList = groupBuilderProps.get(group.repoBuilderProps.get("groupId"));
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
        private final Map<String, String> repoBuilderProps = new LinkedHashMap<>();
        private final List<String> repoPatternProps = new ArrayList<>();
        private final ExternalBuilders externalBuilders = new ExternalBuilders();
        private GroupBuilder parent;

        public RepoBuilder() {
        }

        RepoBuilder groupId(String groupId) {
            repoBuilderProps.put("groupId", groupId);
            return this;
        }

        RepoBuilder repoPattern(String repoPattern) {
            repoPatternProps.add(repoPattern);
            return this;
        }

        RepoBuilder branch(String branchPattern) {
            repoBuilderProps.put("branchPattern", branchPattern);
            return this;
        }

        RepoBuilder displayName(String displayName) {
            repoBuilderProps.put("displayName", displayName);
            return this;
        }

        RepoBuilder description(String description) {
            repoBuilderProps.put("description", description);
            return this;
        }

        RepoBuilder defaultGroupRepo(String defaultGroupRepo) {
            repoBuilderProps.put("defaultGroupRepo", defaultGroupRepo);
            return this;
        }

        RepoBuilder withExternal(ExternalBuilder<?> externalBuilder) {
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

        Repo build() {
            Repo repo = new Repo(parent.organization, repoPatternProps, repoBuilderProps.get("branchPattern"), repoBuilderProps.get("groupId"),
                    repoBuilderProps.get("displayName"), repoBuilderProps.get("description"), repoBuilderProps.get("defaultGroupRepo"));

            for (ExternalBuilder<?> externalBuilder : externalBuilders.externalBuilderProps.values()) {
                Object build = externalBuilder.build();
                repo.services.put(build.getClass(), build);
            }

            return repo;
        }
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
        private static final long serialVersionUID = -5456273290552851349L;

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
        private static final long serialVersionUID = -440454858480551064L;

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
}
