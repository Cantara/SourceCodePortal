package no.cantara.docsite.json;

import javax.json.JsonValue;
import java.util.Arrays;
import java.util.Deque;
import java.util.stream.Collectors;

public class JsonTraversalElement {

    public final JsonTraversalElement parent;
    public final String key;
    public final JsonValue value;
    public final int arrayPos;

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

        path[i++] = (isArrayElement() ? value.toString() : key);
//        path[i++] = key;
        return path;
    }

    public int depth(Deque<JsonTraversalElement> ancestors) {
        return ancestors.size()-1;
    }

    public String path(Deque<JsonTraversalElement> ancestors) {
        return Arrays.asList(uri(ancestors)).stream().collect(Collectors.joining("/"));
    }

    public boolean isArrayElement() {
        return arrayPos > -1;
    }

    @Override
    public String toString() {
        return "JsonTraversalElement{" +
                "key='" + key + '\'' +
                ", arrayPos=" + arrayPos +
                ", isArrayElement=" + isArrayElement()+
                '}';
    }
}
