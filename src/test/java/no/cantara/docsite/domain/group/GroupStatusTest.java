package no.cantara.docsite.domain.group;

import org.testng.annotations.Test;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashSet;
import java.util.Set;

public class GroupStatusTest {

    @Test
    public void testGroupAggregation() {
        Set<NewRepo> group = initializeGroup();

    }


    private Set<NewRepo> initializeGroup() {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        .withZone(ZoneId.systemDefault());
        Set<NewRepo> group = new HashSet<>();

        RepositoryStatus buildstatus = new RepositoryStatus().withTerminology("build").withValue("true");
        RepositoryStatus commitStatus = new RepositoryStatus().withTerminology("commit").withValue("true");

        NewRepo repo = new NewRepo();
        repo.addToRepoStatus(buildstatus);
        repo.addToRepoStatus(commitStatus);
        group.add(repo);

        RepositoryStatus buildstatus2 = new RepositoryStatus().withTerminology("build").withValue("true");
        RepositoryStatus commitStatus2 = new RepositoryStatus().withTerminology("commit").withValue("true");

        NewRepo repo2 = new NewRepo();
        repo2.addToRepoStatus(buildstatus2);
        repo2.addToRepoStatus(commitStatus2);
        group.add(repo2);

        return group;
    }

}


