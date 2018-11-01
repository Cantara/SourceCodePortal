package no.cantara.docsite.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.BiConsumer;

public class JsonDocumentTraversal {

    private static final Logger LOG = LoggerFactory.getLogger(JsonDocumentTraversal.class);

    private final Deque<JsonTraversalElement> ancestors = new LinkedList<>();
    private final BiConsumer<Deque<JsonTraversalElement>, JsonTraversalElement> visitor;

    JsonDocumentTraversal(JsonObject jsonObject, BiConsumer<Deque<JsonTraversalElement>, JsonTraversalElement> visitor) {
        this.visitor = visitor;
        walkJsonObject(jsonObject);
    }

    public static void walk(JsonObject jsonDocument, BiConsumer<Deque<JsonTraversalElement>, JsonTraversalElement> visitor) {
        new JsonDocumentTraversal(jsonDocument, visitor);
    }

    private void walkJsonObject(JsonObject jsonObject) {
        for (String key : jsonObject.keySet()) {
            JsonValue value = jsonObject.get(key);

            if (value.getValueType() == JsonValue.ValueType.OBJECT) {
                JsonTraversalElement te = new JsonTraversalElement(key, value);
                visitor.accept(ancestors, te);
                ancestors.addLast(te);
                walkJsonObject((JsonObject) value);
                ancestors.removeLast();

            } else if (value.getValueType() == JsonValue.ValueType.ARRAY) {
                JsonTraversalElement te = new JsonTraversalElement(key, value);
                visitor.accept(ancestors, te);
                Integer pos = 0;
                JsonArray arrayObject = value.asJsonArray();
                for (Iterator<JsonValue> it = arrayObject.iterator(); it.hasNext(); ) {
                    JsonValue arrayElement = it.next();

                    if (arrayElement.getValueType() == JsonValue.ValueType.OBJECT) {
                        ancestors.addLast(te);
                        ancestors.addLast(new JsonTraversalElement(pos.toString(), null));
                        walkJsonObject(arrayElement.asJsonObject());
                        ancestors.removeLast();
                        ancestors.removeLast();

                    } else if (arrayElement.getValueType() == JsonValue.ValueType.ARRAY) {
                        throw new UnsupportedOperationException("Unsupported data type: " + arrayElement.getClass());

                    } else {
                        throw new UnsupportedOperationException("Unsupported data type: " + arrayElement.getClass());
                    }
                    pos++;
                }

            } else {
                JsonTraversalElement te = new JsonTraversalElement(key, value);
                visitor.accept(ancestors, te);
            }
        }
    }
}
