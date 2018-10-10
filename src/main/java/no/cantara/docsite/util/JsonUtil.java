package no.cantara.docsite.util;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.bind.JsonbBuilder;
import javax.json.stream.JsonGenerator;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    public static String prettyPrint(JsonStructure json) {
        return jsonFormat(json, JsonGenerator.PRETTY_PRINTING);
    }

    public static String jsonFormat(JsonStructure json, String... options) {
        StringWriter stringWriter = new StringWriter();
        Map<String, Boolean> config = buildConfig(options);
        JsonWriterFactory writerFactory = Json.createWriterFactory(config);
        JsonWriter jsonWriter = writerFactory.createWriter(stringWriter);

        jsonWriter.write(json);
        jsonWriter.close();

        return stringWriter.toString();
    }

    private static Map<String, Boolean> buildConfig(String... options) {
        Map<String, Boolean> config = new HashMap<>();
        if (options != null) {
            for (String option : options) {
                config.put(option, true);
            }
        }

        return config;
    }

    public static JsonObject asJsonObject(Object json) {
        try (JsonReader reader = Json.createReader(new StringReader(json.toString()))) {
            return reader.readObject();
        }
    }

    public static JsonArray asJsonArray(Object json) {
        try (JsonReader reader = Json.createReader(new StringReader(json.toString()))) {
            return reader.readArray();
        }
    }

    public static String asString(Object jsonObject) {
        String json = JsonbBuilder.create().toJson(jsonObject);
        return prettyPrint(asJsonObject(json));
    }

}
