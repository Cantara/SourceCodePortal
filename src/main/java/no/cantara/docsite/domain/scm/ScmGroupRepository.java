package no.cantara.docsite.domain.scm;

public class ScmGroupRepository {

    public final ScmRepository repository;
    public final String displayName;
    public final String description;
    public final int numberOfRepos;

    public ScmGroupRepository(ScmRepository repository, String displayName, String description, int numberOfRepos) {
        this.repository = repository;
        this.displayName = displayName;
        this.description = description;
        this.numberOfRepos = numberOfRepos;
    }
}
