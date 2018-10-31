package no.cantara.docsite.domain.links;

public class ShieldsIOReposURL extends LinkURL<String> {

    private static final long serialVersionUID = 6181011109234091890L;
    public static final String KEY = "shieldRepos";

    public ShieldsIOReposURL(String internal) {
        super(internal);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getExternalURL() {
        return "https://img.shields.io/badge/repos-0-lightgrey.svg";
    }

    public String getNumberOfReposURL(int number) {
        return String.format("https://img.shields.io/badge/repos-%s-blue.svg", number);
    }
}
