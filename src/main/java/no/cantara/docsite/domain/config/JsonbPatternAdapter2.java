package no.cantara.docsite.domain.config;

import javax.json.bind.adapter.JsonbAdapter;
import java.util.regex.Pattern;

public class JsonbPatternAdapter2 implements JsonbAdapter<Pattern,String> {

    @Override
    public String adaptToJson(Pattern obj) throws Exception {
        return obj.pattern();
    }

    @Override
    public Pattern adaptFromJson(String obj) throws Exception {
        throw new UnsupportedOperationException();
    }
}
