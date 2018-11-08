package no.cantara.docsite.domain.shields;

import java.io.Serializable;

public class ShieldsStatus implements Serializable {

    private static final long serialVersionUID = 2398352983485925821L;

    public final String svg;
    public final ShieldsBadge status;

    public ShieldsStatus(String svg, ShieldsBadge status) {
        this.svg = svg;
        this.status = status;
    }

}
