package no.cantara.docsite.domain.config;

import no.cantara.docsite.domain.github.repos.GetGitHubRepositories;
import no.cantara.docsite.model.config.RepositoryConfig;
import no.cantara.docsite.model.github.pull.GitHubRepository;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyNamingStrategy;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepositoryConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryConfigLoader.class);

    private final DynamicConfiguration configuration;
    public final RepositoryConfig repositoryConfig;

    public RepositoryConfigLoader(DynamicConfiguration configuration) {
        this.configuration = configuration;
        repositoryConfig = load();
    }

    private RepositoryConfig load() {
        try (InputStream json = ClassLoader.getSystemResourceAsStream("config/config.json")) {
            JsonbConfig config = new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES);
            Jsonb jsonb = JsonbBuilder.create(config);
            return jsonb.fromJson(json, RepositoryConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String,Group> build() {
        GetGitHubRepositories repos = new GetGitHubRepositories(configuration, repositoryConfig.gitHub.organization);
        List<GitHubRepository> result = repos.getOrganizationRepos();
        Builder builder = new Builder();
        for(RepositoryConfig.Repo repoConfig : repositoryConfig.gitHub.repos) {
            Group group = builder.add(repoConfig.groupId, repositoryConfig.gitHub.organization);
            Pattern pattern = Pattern.compile(repoConfig.repo);
            for (GitHubRepository repo : result) {
                String repoName = repo.name;
                Matcher matcher = pattern.matcher(repoName);
                if (matcher.find()) {
                    String rawRepoURL = String.format("https://raw.githubusercontent.com/%s/%s/%s/", repositoryConfig.gitHub.organization, repoName, repoConfig.branch);
                    String readmeURL = String.format("https://api.github.com/repos/%s/%s/readme?ref=%s", repositoryConfig.gitHub.organization, repoName, repoConfig.branch);
                    String contentsURL = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", repositoryConfig.gitHub.organization, repoName, "%s", repoConfig.branch);
                    group.add(repoName, new Repository(repoName, repoConfig.branch, repo.htmlUrl, rawRepoURL, readmeURL, contentsURL));
                }
            }
        }
        return builder.build();
    }

    public static class Builder {
        private final Map<String,Group> groupMap = new LinkedHashMap<>();

        public Group add(String groupId, String organization) {
            Group group = new Group(this, groupId, organization);
            groupMap.put(groupId, group);
            return group;
        }

        Map<String,Group> build() {
            return groupMap;
        }
    }

    public static class Group {
        private final Builder builder;
        private final Map<String,Repository> repositoryMap = new LinkedHashMap<>();
        public final String groupId;
        public final String organization;

        public Group(Builder builder, String groupId, String organization) {
            this.builder = builder;
            this.groupId = groupId;
            this.organization = organization;
        }

        public Group add(String repoName, Repository repository) {
            repositoryMap.put(repoName, repository);
            return this;
        }

        public Map<String, Repository> getRepositories() {
            return repositoryMap;
        }

        Builder up() {
            return builder;
        }
    }

    public static class Repository {
        public final String repoName;
        public final String branch;
        public final String repoURL;
        public final String rawRepoURL;
        public final String readmeURL;
        public final String contentsURL;

        public Repository(String repoName, String branch, String repoURL, String rawRepoURL, String readmeURL, String contentsURL) {
            this.repoName = repoName;
            this.branch = branch;
            this.repoURL = repoURL;
            this.rawRepoURL = rawRepoURL;
            this.readmeURL = readmeURL;
            this.contentsURL = contentsURL;
        }
    }

}
