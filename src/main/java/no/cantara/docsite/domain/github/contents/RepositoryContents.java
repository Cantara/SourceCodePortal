package no.cantara.docsite.domain.github.contents;

import no.cantara.docsite.util.JsonUtil;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbTypeAdapter;
import java.io.Serializable;

public class RepositoryContents implements Serializable {

    private static final long serialVersionUID = 3900913417928771858L;

    public String type;
    public String encoding;
    public int size;
    public String name;
    public String path;
    public @JsonbTypeAdapter(Base64MimeAdapter.class) String content;
    public String sha;
    public String url;
    public @JsonbProperty("git_url") String gitUrl;
    public @JsonbProperty("html_url") String htmlUrl;
    public @JsonbProperty("download_url") String downloadUrl;
    public @JsonbProperty("_links") Links links;
    public @JsonbTransient String renderedHtml;

    @Override
    public String toString() {
        return JsonUtil.asString(this);
    }

    public static class Links implements Serializable {

        private static final long serialVersionUID = 1084824923791038625L;

        public String git;
        public String self;
        public String html;
    }

}
