package no.cantara.docsite.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class URLRewriter {

    private String url;

    public URLRewriter(String url) {
        this.url = url;
    }

    public String toURI() {
        try {
            URL url = new URL(this.url);
            return url.toURI().getRawPath();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
