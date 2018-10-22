package no.cantara.docsite.domain.github.contents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.adapter.JsonbAdapter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64MimeAdapter implements JsonbAdapter<String,String> {

    private static final Logger LOG = LoggerFactory.getLogger(Base64MimeAdapter.class);

    @Override
    public String adaptToJson(String obj) throws Exception {
//        return new String(Base64.getMimeEncoder().encode(obj.getBytes()), StandardCharsets.UTF_8);
        return obj;
    }

    @Override
    public String adaptFromJson(String obj) throws Exception {
        return new String(Base64.getMimeDecoder().decode(obj), StandardCharsets.UTF_8);
    }

}
