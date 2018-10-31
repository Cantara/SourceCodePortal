package no.cantara.docsite.cache;

import no.cantara.docsite.util.JsonbFactory;

import java.io.Serializable;
import java.util.Objects;

public class CacheGroupKey implements Serializable {

    private static final long serialVersionUID = 8180456028164081124L;

    public final String organization;
    public final String groupId;

    CacheGroupKey(String organization, String groupId) {
        this.organization = organization;
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheGroupKey)) return false;
        CacheGroupKey that = (CacheGroupKey) o;
        return Objects.equals(organization, that.organization) &&
                Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organization, groupId);
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }

    public static CacheGroupKey of(String organization, String groupId) {
        return new CacheGroupKey(organization, groupId);
    }

}
