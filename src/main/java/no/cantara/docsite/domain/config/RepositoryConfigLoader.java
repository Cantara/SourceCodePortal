package no.cantara.docsite.domain.config;

import no.cantara.docsite.cache.CacheGroupKey;
import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.github.repos.GetGitHubRepositories;
import no.cantara.docsite.domain.github.repos.GitHubRepository;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyNamingStrategy;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepositoryConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryConfigLoader.class);

    private final DynamicConfiguration configuration;
    private final CacheStore cacheStore;
    public final RepositoryConfig repositoryConfig;

    public RepositoryConfigLoader(DynamicConfiguration configuration, CacheStore cacheStore) {
        this.configuration = configuration;
        this.cacheStore = cacheStore;
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

    public void build() {
        GetGitHubRepositories repos = new GetGitHubRepositories(configuration, repositoryConfig.gitHub.organization);
        List<GitHubRepository> result = repos.getOrganizationRepos();

        for(RepositoryConfig.Repo repoConfig : repositoryConfig.gitHub.repos) {
            LOG.trace("repoConfig: {}", repoConfig);
            Pattern pattern = Pattern.compile(repoConfig.repo);
            for (GitHubRepository repo : result) {
                LOG.trace("repo: {}", repo.name);
                String repoName = repo.name;
                Matcher matcher = pattern.matcher(repoName);
                if (matcher.find()) {
                    CacheKey cacheKey = CacheKey.of(repositoryConfig.gitHub.organization, repoName, repoConfig.branch);
                    String rawRepoURL = String.format("https://raw.githubusercontent.com/%s/%s/%s/", cacheKey.organization, cacheKey.repoName, cacheKey.branch);
                    String readmeURL = String.format("https://api.github.com/repos/%s/%s/readme?ref=%s", cacheKey.organization, cacheKey.repoName, cacheKey.branch);
                    String contentsURL = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", cacheKey.organization, cacheKey.repoName, "%s", cacheKey.branch);
                    // TODO make a cache object that includes CacheKey and ULRs
                    CacheGroupKey cacheGroupKey = CacheGroupKey.of(cacheKey, repoConfig.groupId);
                    no.cantara.docsite.domain.config.Repository repository = new no.cantara.docsite.domain.config.Repository(cacheKey, repo.htmlUrl, rawRepoURL, readmeURL, contentsURL);
                    cacheStore.getRepositoryGroups().put(cacheGroupKey, repository);
                }
            }
        }
    }
}
