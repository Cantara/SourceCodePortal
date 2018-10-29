package no.cantara.docsite.domain.github.commits;


import no.cantara.docsite.cache.CacheShaKey;
import no.cantara.docsite.domain.scm.ScmCommitRevision;
import no.cantara.docsite.util.JsonbFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CommitRevisionBinding implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(CommitRevisionBinding.class);

    private static final long serialVersionUID = -5773578164096532597L;

    public String sha;
    public Commit commit;
    public @JsonbProperty("html_url") String htmlUrl;
    public Author author;
    public List<Parent> parents = new ArrayList<>();


    @Deprecated
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

    @JsonbTransient
    public ScmCommitRevision asCommitRevision(CacheShaKey cacheShaKey) {
        return new ScmCommitRevision(cacheShaKey,
                (commit.commitAuthor == null ? null : commit.commitAuthor.name),
                (commit.commitAuthor == null ? null : commit.commitAuthor.email),
                (commit.commitAuthor == null ? null : commit.commitAuthor.date),
                htmlUrl,
                (author == null ? null : author.avatarUrl),
                commit.message,
                parents.stream().map(m -> new ScmCommitRevision.Parent(m.sha, m.url, m.htmlUrl)).collect(Collectors.toList()));
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
