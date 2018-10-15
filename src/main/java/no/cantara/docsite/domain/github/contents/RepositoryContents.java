package no.cantara.docsite.domain.github.contents;

import no.cantara.docsite.util.JsonUtil;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RepositoryContents implements Serializable {

    private static final long serialVersionUID = 3900913417928771858L;

    public String type;
    public String encoding;
    public int size;
    public String name;
    public String path;
    public String content;
    public String sha;
    public String url;
    public @JsonbProperty("git_url") String gitUrl;
    public @JsonbProperty("html_url") String htmlUrl;
    public @JsonbProperty("download_url") String downloadUrl;
    public @JsonbProperty("_links") Links links;

    @JsonbTransient
    public String getDecodedContent() {
        if (encoding == null || !"base64".equals(encoding) || content == null) {
            return content;
        }
        return new String(Base64.getMimeDecoder().decode(content), StandardCharsets.UTF_8);
    }

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
