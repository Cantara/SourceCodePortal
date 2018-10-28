package no.cantara.docsite.domain.config;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.util.JsonbFactory;

import java.io.Serializable;
import java.util.Objects;

@Deprecated
public class Repository implements Serializable {

    public static final String SCP_TEMPLATE_REPO_NAME = "ConfigService";
    public static final String SCP_TEMPLATE_ORGANIZATION_NAME = "Cantara";
    public static final String SCP_TEMPLATE_JENKINS_URL = "https://jenkins.capraconsulting.no";

    private static final long serialVersionUID = 3135783516132571382L;

    public final CacheKey cacheKey;
    public final String id;
    public final String name;
    public final String defaultGroupRepo;
    public final String description;
    public final String repoURL;
    public final String rawRepoURL;
    public final String readmeURL;
    public final String contentsURL;
    public String jenkinsURL = SCP_TEMPLATE_JENKINS_URL + "/buildStatus/icon?job=" + SCP_TEMPLATE_REPO_NAME;
    public String groupCommit = "https://img.shields.io/github/last-commit/" + SCP_TEMPLATE_ORGANIZATION_NAME + "/" + SCP_TEMPLATE_REPO_NAME + ".svg";
    public String groupStatus = "unknown";
    public String groupRelease = "https://img.shields.io/github/tag/" + SCP_TEMPLATE_ORGANIZATION_NAME + "/" + SCP_TEMPLATE_REPO_NAME + ".svg";
    public String no_repos = "https://img.shields.io/badge/repos-5-blue.svg";
    public String snykIOUrl = "https://snyk.io/test/github/" + SCP_TEMPLATE_ORGANIZATION_NAME + "/" + SCP_TEMPLATE_REPO_NAME + "/badge.svg";
    public String snyktestIOUrl = "https://snyk.io/test/github/" + SCP_TEMPLATE_ORGANIZATION_NAME + "/" + SCP_TEMPLATE_REPO_NAME;
    public String githubIssues = "https://img.shields.io/github/issues/" + SCP_TEMPLATE_ORGANIZATION_NAME + "/" + SCP_TEMPLATE_REPO_NAME + ".svg";


    Repository(CacheKey cacheKey, String id, String name, String defaultGroupRepo, String description, String repoURL, String rawRepoURL, String readmeURL, String contentsURL) {
        this.cacheKey = cacheKey;
        this.id = id;
        this.name = name;
        this.defaultGroupRepo = defaultGroupRepo;
        this.description = description;
        this.repoURL = repoURL;
        this.rawRepoURL = rawRepoURL;
        this.readmeURL = readmeURL;
        this.contentsURL = contentsURL;
        if (defaultGroupRepo != null) {
            if (snykIOUrl != null && snykIOUrl.contains(SCP_TEMPLATE_REPO_NAME)) {
                this.snykIOUrl = snykIOUrl.replace(SCP_TEMPLATE_REPO_NAME, defaultGroupRepo);
            }
            this.snyktestIOUrl = snyktestIOUrl.replace(SCP_TEMPLATE_REPO_NAME, defaultGroupRepo);
            this.jenkinsURL = jenkinsURL.replace(SCP_TEMPLATE_REPO_NAME, defaultGroupRepo);
            this.groupRelease = groupRelease.replace(SCP_TEMPLATE_REPO_NAME, defaultGroupRepo);
            this.groupCommit = groupCommit.replace(SCP_TEMPLATE_REPO_NAME, defaultGroupRepo);
            this.githubIssues = githubIssues.replace(SCP_TEMPLATE_REPO_NAME, defaultGroupRepo);
        }
    }

    public void setNoOfRepos(int noOfRepos) {
        String noString = String.valueOf(noOfRepos);
        this.no_repos = no_repos.replaceAll("-5-", "-" + noString + "-");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Repository)) return false;
        Repository that = (Repository) o;
        return Objects.equals(cacheKey, that.cacheKey) &&
                Objects.equals(repoURL, that.repoURL) &&
                Objects.equals(rawRepoURL, that.rawRepoURL) &&
                Objects.equals(readmeURL, that.readmeURL) &&
                Objects.equals(contentsURL, that.contentsURL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cacheKey, repoURL, rawRepoURL, readmeURL, contentsURL);
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }

    public static Repository of(CacheKey cacheKey, String id, String name, String defaultGroupRepo, String description, String repoURL, String rawRepoURL, String readmeURL, String contentsURL) {
        return new Repository(cacheKey, id, name, defaultGroupRepo, description, repoURL, rawRepoURL, readmeURL, contentsURL);
    }

}
