package no.cantara.docsite.cache;

import no.cantara.docsite.util.JsonbFactory;

import java.io.Serializable;
import java.util.Objects;

public class CacheCantaraWikiKey implements Serializable {

    private static final long serialVersionUID = -3831357518065033430L;

    public final String contentId;

    CacheCantaraWikiKey(String contentId) {
        this.contentId = contentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheCantaraWikiKey)) return false;
        CacheCantaraWikiKey that = (CacheCantaraWikiKey) o;
        return Objects.equals(contentId, that.contentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentId);
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }

    public static CacheCantaraWikiKey of(String contentId) {
        return new CacheCantaraWikiKey(contentId);
    }
}
