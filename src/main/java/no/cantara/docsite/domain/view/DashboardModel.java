package no.cantara.docsite.domain.view;

import java.util.LinkedHashSet;
import java.util.Set;

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
        public String groupCommit = "https://img.shields.io/github/last-commit/Cantara/ConfigService.svg";
        public String groupStatus = "unknown";
        public String groupRelease = "https://img.shields.io/github/tag/Cantara/ConfigService.svg";
        public String no_repos = "https://img.shields.io/badge/repos-5-blue.svg";
        public String snykIOUrl = "https://snyk.io/test/github/Cantara/ConfigService/badge.svg";
        public String snyktestIOUrl = "https://snyk.io/test/github/Cantara/ConfigService";

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
            this.snykIOUrl = snykIOUrlx.replaceAll("ConfigService", repoName);
            this.snyktestIOUrl = snyktestIOUrl.replace("ConfigService", repoName);
            this.groupRelease = groupRelease.replace("ConfigService", repoName);
            this.groupCommit = groupCommit.replace("ConfigService", repoName);
            //this.snykIOUrl = snykIOUrl;
        }

        public void setNoOfRepos(int noOfRepos) {
            String noString = String.valueOf(noOfRepos);
            this.no_repos = no_repos.replaceAll("-", "-" + noString + "-");
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
