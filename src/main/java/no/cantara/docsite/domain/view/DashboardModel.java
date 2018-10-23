package no.cantara.docsite.domain.view;

import java.util.LinkedHashSet;
import java.util.Set;

import static no.cantara.docsite.domain.config.Repository.SCP_TEMPLATE_ORGANIZATION_NAME;
import static no.cantara.docsite.domain.config.Repository.SCP_TEMPLATE_REPO_NAME;

public class DashboardModel {

    public final Set<Group> groups = new LinkedHashSet<>();

    public DashboardModel() {
    }

    public static class Group {

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
        public final String jenkinsURL;
        public String groupCommit = "https://img.shields.io/github/last-commit/" + SCP_TEMPLATE_ORGANIZATION_NAME + "/" + SCP_TEMPLATE_REPO_NAME + ".svg";
        public String groupStatus = "unknown";
        public String groupRelease = "https://img.shields.io/github/tag/" + SCP_TEMPLATE_ORGANIZATION_NAME + "/" + SCP_TEMPLATE_REPO_NAME + ".svg";
        public String no_repos = "https://img.shields.io/badge/repos-5-blue.svg";
        public String snykIOUrl = "https://snyk.io/test/github/" + SCP_TEMPLATE_ORGANIZATION_NAME + "/" + SCP_TEMPLATE_REPO_NAME + "/badge.svg";
        public String snyktestIOUrl = "https://snyk.io/test/github/" + SCP_TEMPLATE_ORGANIZATION_NAME + "/" + SCP_TEMPLATE_REPO_NAME;

        public Set<Activity> activity = new LinkedHashSet<>();

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
            this.groupCommit = groupCommit.replace(SCP_TEMPLATE_REPO_NAME, repoName);
            //this.snykIOUrl = snykIOUrl;
        }

        public void setNoOfRepos(int noOfRepos) {
            String noString = String.valueOf(noOfRepos);
            this.no_repos = no_repos.replaceAll("-5-", "-" + noString + "-");
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
