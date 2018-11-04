package no.cantara.docsite.cache;

import no.cantara.docsite.json.JsonbFactory;

import java.io.Serializable;
import java.util.Objects;

public class CacheShaKey implements Serializable {

    private static final long serialVersionUID = 3693984252843664753L;

    public final String organization;
    public final String repoName;
    public final String branch;
    public final String groupId;
    public final String sha;

    CacheShaKey(String organization, String repoName, String branch, String groupId, String sha) {
        this.organization = organization;
        this.repoName = repoName;
        this.branch = branch;
        this.groupId = groupId;
        this.sha = sha;
    }

    public boolean compareToUsingRepoName(String organization, String repoName) {
        return organization.equals(this.organization) && repoName.equals(this.repoName);
    }

    public boolean compareToUsingBranch(String organization, String repoName, String branch) {
        return organization.equals(this.organization) && repoName.equals(this.repoName) && branch.equals(this.branch);
    }

    public boolean compareToUsingGroupId(String organization, String groupId) {
        return organization.equals(this.organization) && groupId.equals(this.groupId);
    }

    public boolean compareToUsingRepoName(String organization, String repoName, String branch, String groupId) {
        return organization.equals(this.organization) && repoName.equals(this.repoName) && branch.equals(this.branch) && groupId.equals(this.groupId);
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
        return JsonbFactory.asString(this);
    }

    public static CacheShaKey of(String organization, String repoName, String branch, String groupId, String sha) {
        return new CacheShaKey(organization, repoName, branch, groupId, sha);
    }

    public static CacheShaKey of(CacheKey cacheKey, String groupId, String sha) {
        return new CacheShaKey(cacheKey.organization, cacheKey.repoName, cacheKey.branch, groupId, sha);
    }

}
