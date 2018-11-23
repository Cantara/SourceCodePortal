package no.cantara.docsite.domain.config;

import no.cantara.docsite.domain.links.LinkURL;
import no.ssb.config.DynamicConfiguration;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import java.io.Serializable;

public interface ExternalService<T> extends Serializable {

    String getId();

    @JsonbProperty("badge-prefix") String getPrefix();

    @JsonbTransient Iterable<LinkURL<?>> getLinks(DynamicConfiguration configuration, String organization, String repoName, String branch);

}
