package no.cantara.docsite.cache;

import no.cantara.docsite.util.JsonUtil;

import java.io.Serializable;
import java.util.Objects;

public class CacheGroupKey implements Serializable {

    private static final long serialVersionUID = 9068679113704432070L;

    public final String organization;
    public final String repoName;
    public final String branch;
    public final String groupId;

    CacheGroupKey(String organization, String repoName, String branch, String groupId) {
        this.organization = organization;
        this.repoName = repoName;
        this.branch = branch;
        this.groupId = groupId;
    }

    public boolean compareTo(String groupId) {
        return groupId.equals(this.groupId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheGroupKey)) return false;
        CacheGroupKey that = (CacheGroupKey) o;
        return Objects.equals(organization, that.organization) &&
                Objects.equals(repoName, that.repoName) &&
                Objects.equals(branch, that.branch) &&
                Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organization, repoName, branch, groupId);
    }

    @Override
    public String toString() {
        return JsonUtil.asString(this);
    }

    public CacheKey asCacheKey() {
        return CacheKey.of(organization, repoName, branch);
    }

    public static CacheGroupKey of(CacheKey cacheKey, String groupId) {
        return new CacheGroupKey(cacheKey.organization, cacheKey.repoName, cacheKey.branch, groupId);
    }

    public static CacheGroupKey of(String organization, String repoName, String branch, String groupId) {
        return new CacheGroupKey(organization, repoName, branch, groupId);
    }
}
