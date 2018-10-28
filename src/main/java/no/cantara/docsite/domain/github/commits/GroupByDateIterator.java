package no.cantara.docsite.domain.github.commits;

import no.cantara.docsite.domain.scm.CommitRevision;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class GroupByDateIterator implements Iterator<CommitRevision> {

    private final List<CommitRevision> entries;
    private final ListIterator<CommitRevision> listIterator;
    private CommitRevision last;
    private CommitRevision current;

    public GroupByDateIterator(List<CommitRevision> entries) {
        this.entries = entries;
        this.listIterator = entries.listIterator();
    }

    @Override
    public boolean hasNext() {
        return listIterator.hasNext();
    }

    @Override
    public CommitRevision next() {
        last = current;
        current = listIterator.next();
        return current;
    }

    private LocalDate onlyDate(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year  = localDate.getYear();
        int month = localDate.getMonthValue();
        int day   = localDate.getDayOfMonth();
        return LocalDate.of(year, month, day);
    }

    public boolean isNewDateGroup() {
        if (last == null && current != null) {
            return true; // first entry - new start date

        } else if (last != null && current != null) {
            LocalDate lastDate = onlyDate(last.date);
            LocalDate currDate = onlyDate(current.date);
            return lastDate.compareTo(currDate) > 0;
        }
        return false;

    }

    public int size() {
        return entries.size();
    }

}
