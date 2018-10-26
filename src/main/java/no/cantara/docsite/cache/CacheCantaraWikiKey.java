package no.cantara.docsite.cache;

import no.cantara.docsite.util.JsonbFactory;

import java.io.Serializable;
import java.util.Objects;

public class CacheCantaraWikiKey implements Serializable {

    private static final long serialVersionUID = -3831357518065033430L;

    public final String pageName;
    public final String contentId;

    CacheCantaraWikiKey(String pageName, String contentId) {
        this.pageName = pageName;
        this.contentId = contentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheCantaraWikiKey)) return false;
        CacheCantaraWikiKey that = (CacheCantaraWikiKey) o;
        return Objects.equals(pageName, that.pageName) &&
                Objects.equals(contentId, that.contentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageName, contentId);
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }

    public static CacheCantaraWikiKey of(String pageName, String contentId) {
        return new CacheCantaraWikiKey(pageName, contentId);
    }
}
