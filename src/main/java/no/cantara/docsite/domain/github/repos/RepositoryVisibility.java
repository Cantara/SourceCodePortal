package no.cantara.docsite.domain.github.repos;

public enum RepositoryVisibility {

    ALL("all"),
    PUBLIC("public"),
    PRIVATE("private");

    private final String value;

    RepositoryVisibility(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
