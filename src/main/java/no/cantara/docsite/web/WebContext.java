package no.cantara.docsite.web;

import java.util.Objects;

public class WebContext {

    public final String uri;             // /index /home
    public final String subContext;      // ""           contents
    public boolean exactMatch;
    public final String pageTemplate;    // index.html   home.html
    public final WebHandler webHandler;

    WebContext(String uri, String subContext, boolean exactMatch, String pageTemplate, WebHandler webHandler) {
        this.uri = uri;
        this.subContext = subContext;
        this.exactMatch = exactMatch;
        this.pageTemplate = pageTemplate;
        this.webHandler = webHandler;
    }

    public String asTemplateResource() {
        return String.format("%s/%s", (subContext == null || "".equals(subContext) ? "" : "/" + subContext) , pageTemplate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WebContext)) return false;
        WebContext that = (WebContext) o;
        return exactMatch == that.exactMatch &&
                Objects.equals(uri, that.uri) &&
                Objects.equals(subContext, that.subContext) &&
                Objects.equals(pageTemplate, that.pageTemplate) &&
                Objects.equals(webHandler, that.webHandler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, subContext, exactMatch, pageTemplate, webHandler);
    }

    @Override
    public String toString() {
        return "WebContext{" +
                "uri='" + uri + '\'' +
                ", subContext='" + subContext + '\'' +
                ", exactMatch=" + exactMatch +
                ", pageTemplate='" + pageTemplate + '\'' +
                ", webHandler=" + webHandler +
                '}';
    }

    public static WebContext of(String uri, String subContext, boolean exactMatch, String pageTemplate, WebHandler webHandler) {
        return new WebContext(uri, subContext, exactMatch, pageTemplate, webHandler);
    }

}
