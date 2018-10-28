package no.cantara.docsite.domain.external;

import java.io.Serializable;
import java.util.Objects;

abstract public class ExternalURL<T> implements Serializable {

    protected final T internal;

    public ExternalURL(T internal) {
        this.internal = internal;
    }

    public static ExternalURL<String> VOID() {
        return new ExternalURL<>("") {
            @Override
            public String getKey() {
                return "void";
            }

            @Override
            public String getExternalURL() {
                return "";
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExternalURL)) return false;
        ExternalURL<?> that = (ExternalURL<?>) o;
        return Objects.equals(internal, that.internal) &&
                Objects.equals(getKey(), that.getKey()) &&
                Objects.equals(getExternalURL(), that.getExternalURL());
    }

    @Override
    public int hashCode() {
        return Objects.hash(internal, getKey(), getExternalURL());
    }

    abstract public String getKey();

    abstract public String getExternalURL();

    // TODO should this simply output the externalURL?
    @Override
    public String toString() {
        return "ExternalURL{" +
                "internal=" + internal +
                ", key=" + getKey() +
                ", externalURL=" + getExternalURL() +
                '}';
    }
}
