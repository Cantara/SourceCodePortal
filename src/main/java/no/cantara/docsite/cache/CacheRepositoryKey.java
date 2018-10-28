package no.cantara.docsite.cache;

import no.cantara.docsite.util.JsonbFactory;

import java.io.Serializable;
import java.util.Objects;

public class CacheRepositoryKey implements Serializable {

    private static final long serialVersionUID = 9068679113704432070L;

    public final String organization;
    public final String repoName;
    public final String branch;
    public final String groupId;

    CacheRepositoryKey(String organization, String repoName, String branch, String groupId) {
        this.organization = organization;
        this.repoName = repoName;
        this.branch = branch;
        this.groupId = groupId;
    }

    public boolean compareTo(String groupId) {
        return groupId.equalsIgnoreCase(this.groupId);
    }

    public String getRepoName() {
        return repoName;
    }

    public String getGroupId() {
        return groupId;
    }

    public String asIdentifier() {
        return String.format("%s/%s/%s", organization, repoName, branch);
    }

    public CacheGroupKey asCacheGroupKey() {
        return CacheGroupKey.of(organization, groupId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheRepositoryKey)) return false;
        CacheRepositoryKey that = (CacheRepositoryKey) o;
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
        return JsonbFactory.asString(this);
    }

    public CacheKey asCacheKey() {
        return CacheKey.of(organization, repoName, branch);
    }

    public static CacheRepositoryKey of(CacheKey cacheKey, String groupId) {
        return new CacheRepositoryKey(cacheKey.organization, cacheKey.repoName, cacheKey.branch, groupId);
    }

    public static CacheRepositoryKey of(String organization, String repoName, String branch, String groupId) {
        return new CacheRepositoryKey(organization, repoName, branch, groupId);
    }
}
