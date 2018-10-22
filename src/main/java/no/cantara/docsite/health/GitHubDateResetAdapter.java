package no.cantara.docsite.health;

import javax.json.bind.adapter.JsonbAdapter;
import java.util.Date;

public class GitHubDateResetAdapter implements JsonbAdapter<Date,String> {

    @Override
    public String adaptToJson(Date obj) throws Exception {
        return obj.toInstant().toString();
    }

    @Override
    public Date adaptFromJson(String obj) throws Exception {
        return new Date(Long.valueOf(obj) * 1000);
    }
}
