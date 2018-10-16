package no.cantara.docsite.cache;

import no.cantara.docsite.util.JsonUtil;

import java.io.Serializable;
import java.util.Objects;

public class CacheShaKey implements Serializable {

    private static final long serialVersionUID = 3693984252843664753L;

    public final String organization;
    public final String repoName;
    public final String sha;

    CacheShaKey(String organization, String repoName, String sha) {
        this.organization = organization;
        this.repoName = repoName;
        this.sha = sha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheShaKey)) return false;
        CacheShaKey that = (CacheShaKey) o;
        return Objects.equals(sha, that.sha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sha);
    }

    @Override
    public String toString() {
        return JsonUtil.asString(this);
    }

    // valid for lookup values in cache
    public static CacheShaKey of(String sha) {
        return new CacheShaKey(null, null, sha);
    }

    // used to create catchable values
    public static CacheShaKey of(String organization, String repoName, String sha) {
        return new CacheShaKey(organization, repoName, sha);
    }

    public static CacheShaKey of(CacheKey cacheKey, String sha) {
        return new CacheShaKey(cacheKey.organization, cacheKey.repoName, sha);
    }


    public boolean compareTo(String organization, String repoName) {
        return organization.equals(this.organization) && repoName.equals(this.repoName);
    }
}
