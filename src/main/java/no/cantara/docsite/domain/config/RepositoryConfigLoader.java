package no.cantara.docsite.domain.config;

import no.cantara.docsite.cache.CacheGroupKey;
import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.github.repos.GetGitHubRepositories;
import no.cantara.docsite.domain.github.repos.GitHubRepository;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepositoryConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryConfigLoader.class);

    private final DynamicConfiguration configuration;
    private final CacheStore cacheStore;

    public RepositoryConfigLoader(DynamicConfiguration configuration, CacheStore cacheStore) {
        this.configuration = configuration;
        this.cacheStore = cacheStore;
    }

    public void load() {
        GetGitHubRepositories repos = new GetGitHubRepositories(configuration, cacheStore.getRepositoryConfig().gitHub.organization);
        List<GitHubRepository> result = repos.getOrganizationRepos();

        for(RepositoryConfig.Repo repoConfig : cacheStore.getRepositoryConfig().gitHub.repos) {
            Pattern pattern = Pattern.compile(repoConfig.repo);
            for (GitHubRepository repo : result) {
                String repoName = repo.name;
                Matcher matcher = pattern.matcher(repoName);
                if (matcher.find()) {
                    CacheKey cacheKey = CacheKey.of(cacheStore.getRepositoryConfig().gitHub.organization, repoName, repoConfig.branch);
                    String rawRepoURL = String.format("https://raw.githubusercontent.com/%s/%s/%s/", cacheKey.organization, cacheKey.repoName, cacheKey.branch);
                    String readmeURL = String.format("https://api.github.com/repos/%s/%s/readme?ref=%s", cacheKey.organization, cacheKey.repoName, cacheKey.branch);
                    String contentsURL = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", cacheKey.organization, cacheKey.repoName, "%s", cacheKey.branch);
                    CacheGroupKey cacheGroupKey = CacheGroupKey.of(cacheKey, repoConfig.groupId);
                    cacheStore.getCacheKeys().put(cacheKey, cacheGroupKey);
                    Repository repository = new Repository(cacheKey, repo.htmlUrl, rawRepoURL, readmeURL, contentsURL);
                    cacheStore.getRepositoryGroups().put(cacheGroupKey, repository);
                }
            }
        }
    }
}
