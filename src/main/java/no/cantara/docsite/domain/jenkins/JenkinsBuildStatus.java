package no.cantara.docsite.domain.jenkins;

import java.io.Serializable;

public class JenkinsBuildStatus implements Serializable {

    private static final long serialVersionUID = 3364846208320488296L;

    public final String svg;
    public final JenkinsBuildStatusBadge status;

    public JenkinsBuildStatus(String svg, JenkinsBuildStatusBadge status) {
        this.svg = svg;
        this.status = status;
    }

}
