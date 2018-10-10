package no.cantara.docsite.github;

import no.cantara.docsite.util.JsonUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "project")
public class MavenPom {

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

    public static class Parent {
        public String artifactId;
        public String groupId;
        public String version;
    }

    public static class Modules {
        public @XmlElement(name = "module") List<String> moduleList = new ArrayList<>();
    }

}
