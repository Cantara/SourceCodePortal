package no.cantara.docsite.domain.view;

import no.cantara.docsite.domain.github.commits.CommitRevisionBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class CardModel {

    public final List<CommitRevisionBinding> lastCommitRevisions = new ArrayList();
    public final SortedSet<DashboardModel.Group> groups = new TreeSet<>();

}
