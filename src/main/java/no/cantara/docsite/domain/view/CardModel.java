package no.cantara.docsite.domain.view;

import no.cantara.docsite.domain.github.commits.CommitRevision;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class CardModel {

    public final List<CommitRevision> lastCommitRevisions = new ArrayList();
    public final SortedSet<DashboardModel.Group> groups = new TreeSet<>();

}
