package no.cantara.docsite.domain.config;

import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.util.JsonUtil;

import java.io.Serializable;
import java.util.Objects;

public class Repository implements Serializable {

    private static final long serialVersionUID = 3135783516132571382L;

    public final CacheKey cacheKey;
    public final String id;
    public final String name;
    public final String description;
    public final String repoURL;
    public final String rawRepoURL;
    public final String readmeURL;
    public final String contentsURL;
    public String jenkinsURL = "unknown";
    public String groupCommit = "unknown";
    public String groupStatus = "unknown";
    public String groupRelease = "unknown";
    public String no_repos = "https://img.shields.io/badge/repos-5-blue.svg";
    public String snykIOUrl = "https://snyk.io/test/github/Cantara/ConfigService/badge.svg";


    Repository(CacheKey cacheKey, String id, String name, String description, String repoURL, String rawRepoURL, String readmeURL, String contentsURL) {
        this.cacheKey = cacheKey;
        this.id = id;
        this.name = name;
        this.description = description;
        this.repoURL = repoURL;
        this.rawRepoURL = rawRepoURL;
        this.readmeURL = readmeURL;
        this.contentsURL = contentsURL;
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
        return JsonUtil.asString(this);
    }

    public static Repository of(CacheKey cacheKey, String id, String name, String description, String repoURL, String rawRepoURL, String readmeURL, String contentsURL) {
        return new Repository(cacheKey, id, name, description, repoURL, rawRepoURL, readmeURL, contentsURL);
    }

}
