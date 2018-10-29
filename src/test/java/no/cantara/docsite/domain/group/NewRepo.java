package no.cantara.docsite.domain.group;

import java.util.HashSet;
import java.util.Set;

public class NewRepo {


    Set<RepositoryStatus> repositoryStatusSet = new HashSet<>();

    public void addToRepoStatus(RepositoryStatus rs) {
        repositoryStatusSet.add(rs);
    }

    public Set<RepositoryStatus> getRepositoryStatusSet() {
        return repositoryStatusSet;
    }
}
