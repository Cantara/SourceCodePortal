package no.cantara.docsite.domain.view;

import no.cantara.docsite.domain.scm.RepositoryContents;
import no.cantara.docsite.domain.scm.RepositoryDefinition;

import java.util.SortedSet;
import java.util.TreeSet;

public class ContentsModel {

    public final RepositoryDefinition repository;
    public final RepositoryContents contents;
    public final String contentHtml;

    public SortedSet<Group> groups = new TreeSet<>();

    public ContentsModel(RepositoryDefinition repository, RepositoryContents contents, String contentHtml) {
        this.repository = repository;
        this.contents = contents;
        this.contentHtml = contentHtml;
    }

    public static class Group implements Comparable<Group> {
        public final String organization;
        public final String repoName;
        public final String branch;
        public final String groupId;
        public final String displayName;
        public final String description;
        public final boolean hasReadme;
        public final String readmeURI;
        public final String cardURI;

        public Group(String organization, String repoName, String branch, String groupId, String displayName, String description, boolean hasReadme, String readmeURI, String cardURI) {
            this.organization = organization;
            this.repoName = repoName;
            this.branch = branch;
            this.groupId = groupId;
            this.displayName = displayName;
            this.description = description;
            this.hasReadme = hasReadme;
            this.readmeURI = readmeURI;
            this.cardURI = cardURI;
        }

        @Override
        public int compareTo(Group o) {
            return 0;
        }
    }
}
