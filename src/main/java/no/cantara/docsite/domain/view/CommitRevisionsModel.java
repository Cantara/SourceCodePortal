package no.cantara.docsite.domain.view;

import no.cantara.docsite.domain.github.commits.CommitRevisionBinding;

import java.util.Iterator;

public class CommitRevisionsModel {

    public final Iterator<CommitRevisionBinding> commitRevisions;

    public CommitRevisionsModel(Iterator<CommitRevisionBinding> commitRevisions) {
        this.commitRevisions = commitRevisions;
    }
}
