package no.cantara.docsite.domain.view;

import no.cantara.docsite.domain.scm.ScmCommitRevision;

import java.util.Iterator;

public class CommitRevisionsModel {

    public final Iterator<ScmCommitRevision> commitRevisions;

    public CommitRevisionsModel(Iterator<ScmCommitRevision> commitRevisions) {
        this.commitRevisions = commitRevisions;
    }
}
