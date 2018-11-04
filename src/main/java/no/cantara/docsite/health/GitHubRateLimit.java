package no.cantara.docsite.health;

import no.cantara.docsite.json.JsonbFactory;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTypeAdapter;
import java.util.Date;

public class GitHubRateLimit {

    public Resources resources;
    public @JsonbProperty("rate") Limit rate;

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }

    public static class Resources {
        public @JsonbProperty("core") Limit core;
        public @JsonbProperty("search") Limit search;
        public @JsonbProperty("graphql") Limit graphQL;

    }

    public static class Limit {
        public int limit;
        public int remaining;
        public @JsonbTypeAdapter(GitHubDateResetAdapter.class) Date reset;
    }

}
