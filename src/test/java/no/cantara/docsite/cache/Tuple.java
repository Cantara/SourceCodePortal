package no.cantara.docsite.cache;

import java.util.Objects;

public class Tuple {

    private final String a;
    private final String b;

    public Tuple(String a, String b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple)) return false;
        Tuple tuple = (Tuple) o;
        return Objects.equals(a, tuple.a) &&
                Objects.equals(b, tuple.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a) & Objects.hashCode(b);
    }
}
