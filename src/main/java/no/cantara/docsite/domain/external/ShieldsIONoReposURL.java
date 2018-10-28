package no.cantara.docsite.domain.external;

public class ShieldsIONoReposURL extends ExternalURL<String> {

    private static final long serialVersionUID = 6181011109234091890L;
    public static final String KEY = "shieldsNoRepos";

    public ShieldsIONoReposURL(String internal) {
        super(internal);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getExternalURL() {
        return "https://img.shields.io/badge/repos-5-blue.svg";
    }
}
