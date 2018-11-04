package no.cantara.docsite.domain.config;

import javax.json.bind.adapter.JsonbAdapter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JsonbPatternAdapter implements JsonbAdapter<Pattern[],String[]> {

    @Override
    public String[] adaptToJson(Pattern[] obj) throws Exception {
        return List.of(obj).stream().map(m -> m.pattern()).collect(Collectors.toList()).toArray(new String[obj.length]);
    }

    @Override
    public Pattern[] adaptFromJson(String[] obj) throws Exception {
        throw new UnsupportedOperationException();
    }
}
