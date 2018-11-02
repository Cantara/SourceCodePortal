package no.cantara.docsite.json;

import javax.json.JsonValue;
import java.util.Arrays;
import java.util.Deque;
import java.util.stream.Collectors;

public class JsonTraversalElement {

    public final JsonTraversalElement parent;
    public final String key;
    public final JsonValue value;
    private final int arrayPos;
    boolean newSibling;

    public JsonTraversalElement(JsonTraversalElement parent, String key, JsonValue value) {
        this(parent, key, value, -1);
    }

    public JsonTraversalElement(JsonTraversalElement parent, String key, JsonValue value, int arrayPos) {
        this.parent = parent;
        this.key = key;
        this.value = value;
        this.arrayPos = arrayPos;
    }

    static String ensureNoPrefixSlash(Object value) {
        String relativePath = String.valueOf(value);
        return relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
    }

    public String[] uri(Deque<JsonTraversalElement> ancestors) {
        String[] path = new String[ancestors.size() + 1];
        int i = 0;
        for (JsonTraversalElement element : ancestors) {
            path[i++] = element.key;
        }

        path[i++] = key;
        return path;
    }

    public int depth(Deque<JsonTraversalElement> ancestors) {
        return ancestors.size() - 1;
    }

    public String path(Deque<JsonTraversalElement> ancestors) {
        return Arrays.asList(uri(ancestors)).stream().collect(Collectors.joining("/"));
    }

    public int getArrayPos() {
        return (isArrayElement() ? parent.arrayPos : arrayPos);
    }

    public boolean isNewSibling() {
        return newSibling;
    }

    public boolean isArray() {
        return JsonValue.ValueType.ARRAY.equals(value.getValueType()) || isNewSibling();
    }

    public boolean isArrayElement() {
        return (parent != null && parent.arrayPos > -1) || isNewSibling();
    }

    @Override
    public String toString() {
        return "JsonTraversalElement{" +
                "key='" + key + '\'' +
                ", newSibling=" + newSibling +
                ", arrayPos=" + getArrayPos() +
                ", isArray=" + isArray() +
                ", isArrayElement=" + isArrayElement() +
                '}';
    }
}
