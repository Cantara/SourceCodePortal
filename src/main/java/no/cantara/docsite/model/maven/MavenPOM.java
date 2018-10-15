package no.cantara.docsite.model.maven;

import no.cantara.docsite.util.JsonUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "project")
public class MavenPOM implements Serializable {
    private static final long serialVersionUID = -3691809286750806283L;

    public Parent parent;
    public String artifactId;
    public String groupId;
    public String packaging;
    public String version;
    public String name;
    public String description;
    public Modules modules;

    @Override
    public String toString() {
        return JsonUtil.asString(this);
    }

    public static class Parent implements Serializable {
        private static final long serialVersionUID = 830362084909597723L;

        public String artifactId;
        public String groupId;
        public String version;
    }

    public static class Modules implements Serializable {
        private static final long serialVersionUID = 9089319287296524098L;

        public @XmlElement(name = "module") List<String> moduleList = new ArrayList<>();
    }

}
