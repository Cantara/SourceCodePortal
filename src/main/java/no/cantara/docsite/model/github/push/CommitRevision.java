package no.cantara.docsite.model.github.push;


//List<Map<String, Map<String, Map<String, String>>>> elementList = ctx.read("$.[*]");
//        for (Map<String, Map<String, Map<String, String>>> elementMap : elementList) {
//        String shaRevision = String.valueOf(elementMap.get("sha"));
//        Map<String, Map<String, String>> commitMap = elementMap.get("commit");
//        Map<String, String> authorMap = commitMap.get("commitAuthor");
//        String commitAuthor = authorMap.get("name");
//        String date = authorMap.get("date");
//        String message = String.valueOf(commitMap.get("message"));
//        String htmlUrl = String.valueOf(elementMap.get("html_url"));
//        String projectId;
//        try {
//        URL url = new URL(htmlUrl);
//        String[] pathArray = url.getPath().split("\\/");
//        projectId = pathArray[2];
//        } catch (MalformedURLException e) {
//        throw new RuntimeException(e);
//        }
//        String avatarUrl = (elementMap.get("commitAuthor") != null ? String.valueOf(elementMap.get("commitAuthor").get("avatar_url")) : "");
//
//        JSONArray jsonArray = (JSONArray) elementMap.get("parents");
//        Map<String, String> parentsMap = (Map<String, String>) (jsonArray.isEmpty() ? new HashMap<>() : jsonArray.get(0));
//        String shaParentRevision = ((parentsMap != null && !parentsMap.isEmpty()) ? String.valueOf(parentsMap.get("sha")) : null);
//
//        commitRevision.addEntry(new CommitRevision.Entry(projectId, commitRevision.getCurrentPage(), shaRevision, shaParentRevision, commitAuthor, date, message, htmlUrl, avatarUrl));


import no.cantara.docsite.util.JsonUtil;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

public class CommitRevision implements Serializable {

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
        return JsonUtil.asString(this);
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
