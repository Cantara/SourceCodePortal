package no.cantara.docsite.domain.group;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class RepositoryStatus {

    public String terminology;
    public String value;
    public String displayValue;
    public String date;

    DateTimeFormatter formatter =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    .withZone(ZoneId.systemDefault());

    public RepositoryStatus() {
        date = formatter.format(Instant.now());
    }

    public RepositoryStatus withTerminology(String s) {
        this.terminology = s;
        return this;
    }

    public RepositoryStatus withValue(String s) {
        this.value = s;
        return this;
    }

    public RepositoryStatus withDisplayValue(String s) {
        this.displayValue = s;
        return this;
    }

    public RepositoryStatus withDate(String s) {
        this.date = s;
        return this;
    }

}
