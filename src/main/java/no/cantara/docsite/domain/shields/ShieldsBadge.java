package no.cantara.docsite.domain.shields;

import no.cantara.docsite.json.JsonbFactory;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "svg")
public class ShieldsBadge implements Serializable {

    private static final long serialVersionUID = 2278165636236910700L;

    public G g;

    @Override
    public String toString() {
        return JsonbFactory.asString(this);
    }

    public static class G implements Serializable {
        private static final long serialVersionUID = 3361082591376880577L;

        public @XmlElement(name = "text") Set<String> text = new LinkedHashSet<>();
    }
}
