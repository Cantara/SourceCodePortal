package no.cantara.docsite.domain.snyk;

import no.cantara.docsite.json.JsonbFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "svg")
public class SnykTestBadge implements Serializable {

    private static final long serialVersionUID = -3376130182420933847L;

    public G g;

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }

    public static class G implements Serializable {
        private static final long serialVersionUID = 1708531690862339490L;

        public @XmlElement(name = "text") Set<String> text = new LinkedHashSet<>();
    }

}
