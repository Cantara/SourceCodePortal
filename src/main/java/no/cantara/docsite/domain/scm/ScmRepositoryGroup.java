package no.cantara.docsite.domain.scm;

public class ScmRepositoryGroup<T> {

    public final T repository;
    public final String displayName;
    public final String description;
    public final int numberOfRepos;

    public ScmRepositoryGroup(T repository, String displayName, String description, int numberOfRepos) {
        this.repository = repository;
        this.displayName = displayName;
        this.description = description;
        this.numberOfRepos = numberOfRepos;
    }
}
