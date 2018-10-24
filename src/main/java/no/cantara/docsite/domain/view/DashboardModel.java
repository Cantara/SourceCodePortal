package no.cantara.docsite.domain.view;

import no.cantara.docsite.domain.github.commits.CommitRevision;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import static no.cantara.docsite.domain.config.Repository.SCP_TEMPLATE_JENKINS_URL;
import static no.cantara.docsite.domain.config.Repository.SCP_TEMPLATE_ORGANIZATION_NAME;
import static no.cantara.docsite.domain.config.Repository.SCP_TEMPLATE_REPO_NAME;

public class DashboardModel {

    public final List<CommitRevision> lastCommitRevisions = new ArrayList();
    public final SortedSet<Group> groups = new TreeSet<>();

    public DashboardModel() {
    }

    public static class Group implements Comparable<Group> {

        public final String organization;
        public final String repoName;
        public final String defaultGroupRepo;
        public final String branch;
        public final String groupId;
        public final String displayName;
        public final String description;
        public final boolean hasReadme;
        public final String readmeURI;
        public final String cardURI;
        public String jenkinsURL = SCP_TEMPLATE_JENKINS_URL + "/buildStatus/icon?job=" + SCP_TEMPLATE_REPO_NAME;
        public String groupCommit = "https://img.shields.io/github/last-commit/" + SCP_TEMPLATE_ORGANIZATION_NAME + "/" + SCP_TEMPLATE_REPO_NAME + ".svg";
        public String groupStatus = "unknown";
        public String groupRelease = "https://img.shields.io/github/tag/" + SCP_TEMPLATE_ORGANIZATION_NAME + "/" + SCP_TEMPLATE_REPO_NAME + ".svg";
        public String no_repos = "https://img.shields.io/badge/repos-5-blue.svg";
        public String snykIOUrl = "https://snyk.io/test/github/" + SCP_TEMPLATE_ORGANIZATION_NAME + "/" + SCP_TEMPLATE_REPO_NAME + "/badge.svg";
        public String snyktestIOUrl = "https://snyk.io/test/github/" + SCP_TEMPLATE_ORGANIZATION_NAME + "/" + SCP_TEMPLATE_REPO_NAME;
        public String githubIssues = "https://img.shields.io/github/issues/" + SCP_TEMPLATE_ORGANIZATION_NAME + "/" + SCP_TEMPLATE_REPO_NAME + ".svg";

        public SortedSet<Activity> activity = new TreeSet<>();

        public Group(String organization, String repoName, String defaultGroupRepo, String branch, String groupId, String displayName, String description, boolean hasReadme, String readmeURI, String cardURI, String jenkinsURL, String snykIOUrlx) {
            this.organization = organization;
            this.repoName = repoName;
            this.defaultGroupRepo = defaultGroupRepo;
            this.branch = branch;
            this.groupId = groupId;
            this.displayName = displayName;
            this.description = description;
            this.hasReadme = hasReadme;
            this.readmeURI = readmeURI;
            this.cardURI = cardURI;
            this.jenkinsURL = jenkinsURL;
            if (repoName.contains("*") && defaultGroupRepo != null) {
                repoName = defaultGroupRepo;
            }
            this.snykIOUrl = snykIOUrlx.replaceAll(SCP_TEMPLATE_REPO_NAME, repoName);
            this.snyktestIOUrl = snyktestIOUrl.replace(SCP_TEMPLATE_REPO_NAME, repoName);
            this.groupRelease = groupRelease.replace(SCP_TEMPLATE_REPO_NAME, repoName);
            this.githubIssues = githubIssues.replace(SCP_TEMPLATE_REPO_NAME, repoName);
            this.groupCommit = groupCommit.replace(SCP_TEMPLATE_REPO_NAME, repoName);
            //this.snykIOUrl = snykIOUrl;
        }

        public void setNoOfRepos(int noOfRepos) {
            String noString = String.valueOf(noOfRepos);
            this.no_repos = no_repos.replaceAll("-5-", "-" + noString + "-");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Group group = (Group) o;
            return Objects.equals(organization, group.organization) &&
                    Objects.equals(repoName, group.repoName) &&
                    Objects.equals(branch, group.branch) &&
                    Objects.equals(groupId, group.groupId);
        }

        @Override
        public int hashCode() {

            return Objects.hash(organization, repoName, branch, groupId);
        }

        public int compareTo(Group o) {
            return repoName.compareTo(o.repoName);
        }
    }

    public class Activity {
        public final String commit;
        public final String release;
        public final String status;

        public Activity(String commit, String release, String status) {
            this.commit = commit;
            this.release = release;
            this.status = status;
        }
    }

}
