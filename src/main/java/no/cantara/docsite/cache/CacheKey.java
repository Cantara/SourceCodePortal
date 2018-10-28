package no.cantara.docsite.cache;

import no.cantara.docsite.util.JsonbFactory;

import java.io.Serializable;
import java.util.Objects;

public class CacheKey implements Serializable {

    private static final long serialVersionUID = 3693984252843664753L;

    public final String organization;
    public final String repoName;
    public final String branch;

    CacheKey(String organization, String repoName, String branch) {
        this.organization = organization;
        this.repoName = repoName;
        this.branch = branch;
    }

    public String asIdentifier() {
        return String.format("%s/%s/%s", organization, repoName, branch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheKey)) return false;
        CacheKey that = (CacheKey) o;
        return Objects.equals(organization, that.organization) &&
                Objects.equals(repoName, that.repoName) &&
                Objects.equals(branch, that.branch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organization, repoName, branch);
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }

    @Deprecated
    public static CacheKey of(String organization, String repoName) {
        return new CacheKey(organization, repoName, "master");
    }

    public static CacheKey of(String organization, String repoName, String branch) {
        return new CacheKey(organization, repoName, branch);
    }

}
