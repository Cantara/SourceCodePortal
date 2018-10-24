package no.cantara.docsite.domain.view;

import no.cantara.docsite.domain.github.commits.CommitRevision;

import java.util.Iterator;

public class CommitRevisionsModel {

    public final Iterator<CommitRevision> commitRevisions;

    public CommitRevisionsModel(Iterator<CommitRevision> commitRevisions) {
        this.commitRevisions = commitRevisions;
    }
}
