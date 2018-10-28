package no.cantara.docsite.domain.external;

public class GitHubHtmlURL extends ExternalURL<String> {

    private static final long serialVersionUID = 9155136624089496877L;
    public static final String KEY = "gitHubHtmlURL";

    public GitHubHtmlURL(String internal) {
        super(internal);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getExternalURL() {
        return internal;
    }

}
