package no.cantara.docsite.web;

import java.util.Objects;

public class WebContext {

    public final String uri;        // /index /home
    public final String subContext; // ""      docs
    public final WebHandler webHandler;

    WebContext(String uri, String subContext, WebHandler webHandler) {
        this.uri = uri;
        this.subContext = subContext;
        this.webHandler = webHandler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WebContext)) return false;
        WebContext that = (WebContext) o;
        return Objects.equals(uri, that.uri) &&
                Objects.equals(subContext, that.subContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, subContext);
    }

    @Override
    public String toString() {
        return "WebContext{" +
                "uri='" + uri + '\'' +
                ", subContext='" + subContext + '\'' +
                ", webHandler=" + webHandler +
                '}';
    }

    public static WebContext of(String uri, String root, WebHandler webHandler) {
        return new WebContext(uri, root, webHandler);
    }

}
