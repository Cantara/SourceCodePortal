package no.cantara.docsite.domain.snyk;

import java.io.Serializable;

public class SnykTestStatus implements Serializable {

    private static final long serialVersionUID = -6153565147764184667L;

    public final String svg;
    public final SnykTestBadge snykTest;

    public SnykTestStatus(String svg, SnykTestBadge snykTest) {
        this.svg = svg;
        this.snykTest = snykTest;
    }

}
