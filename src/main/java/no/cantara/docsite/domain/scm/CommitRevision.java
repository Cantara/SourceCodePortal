package no.cantara.docsite.domain.scm;

import no.cantara.docsite.cache.CacheShaKey;
import no.cantara.docsite.util.JsonbFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CommitRevision implements Serializable {

    private static final long serialVersionUID = -9181843231543416840L;

    public final CacheShaKey cacheShaKey;
    public final String name;
    public final String email;
    public final Date date;
    public final String htmlUrl;
    public final String avatarUrl;
    public final String message;
    public final List<Parent> parents;

    public CommitRevision(CacheShaKey cacheShaKey, String name, String email, Date date, String htmlUrl, String avatarUrl, String message, List<Parent> parents) {
        this.cacheShaKey = cacheShaKey;
        this.name = name;
        this.email = email;
        this.date = date;
        this.htmlUrl = htmlUrl;
        this.avatarUrl = avatarUrl;
        this.message = message;
        this.parents = parents;
    }

    public static class Parent implements Serializable {
        private static final long serialVersionUID = -8044612709643929945L;

        public final String sha;
        public final String url;
        public final String htmlUrl;

        public Parent(String sha, String url, String htmlUrl) {
            this.sha = sha;
            this.url = url;
            this.htmlUrl = htmlUrl;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommitRevision)) return false;
        CommitRevision that = (CommitRevision) o;
        return Objects.equals(cacheShaKey, that.cacheShaKey) &&
                Objects.equals(name, that.name) &&
                Objects.equals(email, that.email) &&
                Objects.equals(date, that.date) &&
                Objects.equals(htmlUrl, that.htmlUrl) &&
                Objects.equals(avatarUrl, that.avatarUrl) &&
                Objects.equals(message, that.message) &&
                Objects.equals(parents, that.parents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cacheShaKey, name, email, date, htmlUrl, avatarUrl, message, parents);
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }
}
