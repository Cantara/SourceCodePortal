package no.cantara.docsite.domain.scm;

import no.cantara.docsite.util.JsonbFactory;

import java.io.Serializable;
import java.util.Objects;

public class ScmRepositoryContents implements Serializable {

    private static final long serialVersionUID = -9144603141360888994L;

    public final String sha;
    public final String filename;
    public final String type;
    public final String encoding;
    public final String path;
    public final int size;
    public final String content;
    public final String contentUrl;
    public final String scmUrl;
    public final String htmlUrl;
    public final String downloadUrl;

    public ScmRepositoryContents(String sha, String filename, String type, String encoding, String path, int size, String content, String contentUrl, String scmUrl, String htmlUrl, String downloadUrl) {
        this.sha = sha;
        this.filename = filename;
        this.type = type;
        this.encoding = encoding;
        this.path = path;
        this.size = size;
        this.content = content;
        this.contentUrl = contentUrl;
        this.scmUrl = scmUrl;
        this.htmlUrl = htmlUrl;
        this.downloadUrl = downloadUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScmRepositoryContents)) return false;
        ScmRepositoryContents that = (ScmRepositoryContents) o;
        return size == that.size &&
                Objects.equals(sha, that.sha) &&
                Objects.equals(filename, that.filename) &&
                Objects.equals(type, that.type) &&
                Objects.equals(encoding, that.encoding) &&
                Objects.equals(path, that.path) &&
                Objects.equals(content, that.content) &&
                Objects.equals(contentUrl, that.contentUrl) &&
                Objects.equals(scmUrl, that.scmUrl) &&
                Objects.equals(htmlUrl, that.htmlUrl) &&
                Objects.equals(downloadUrl, that.downloadUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sha, filename, type, encoding, path, size, content, contentUrl, scmUrl, htmlUrl, downloadUrl);
    }

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }
}
