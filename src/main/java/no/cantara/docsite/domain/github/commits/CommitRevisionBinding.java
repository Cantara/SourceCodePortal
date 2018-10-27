package no.cantara.docsite.domain.github.commits;


import no.cantara.docsite.util.JsonbFactory;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

public class CommitRevisionBinding implements Serializable {

    private static final long serialVersionUID = -5773578164096532597L;

    public String sha;
    public Commit commit;
    public @JsonbProperty("html_url") String htmlUrl;
    public Author author;
    public List<Parent> parents;


    @JsonbTransient
    public String getProjectId() {
        try {
            URL url = new URL(htmlUrl);
            String[] pathArray = url.getPath().split("\\/");
            return pathArray[2];
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }

    public static class Commit implements Serializable {

        private static final long serialVersionUID = 5045291959738847188L;

        public @JsonbProperty("author") CommitAuthor commitAuthor;
        public String message;
    }

    public static class CommitAuthor implements Serializable {

        private static final long serialVersionUID = -2525412735629228593L;

        public String name;
        public String email;
        public Date date;
    }

    public static class Author implements Serializable {

        private static final long serialVersionUID = -8018559961277889833L;

        public @JsonbProperty("avatar_url") String avatarUrl;
    }

    public static class Parent implements Serializable {

        private static final long serialVersionUID = -3518347737753544401L;

        public String sha;
        public String url;
        public @JsonbProperty("html_url") String htmlUrl;
    }
}
