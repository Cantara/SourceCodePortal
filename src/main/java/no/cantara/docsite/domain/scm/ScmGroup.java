package no.cantara.docsite.domain.scm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScmGroup implements Serializable {

    private static final long serialVersionUID = -7387859290933050966L;

    public final String groupId;
    public final String displayName;
    public final String description;
    public final String defaultEntryRepository;
    private final List<String> repositoryKeys = new ArrayList<>();

    public ScmGroup(String groupId, String displayName, String description, String defaultEntryRepository) {
        this.groupId = groupId;
        this.displayName = displayName;
        this.description = description;
        this.defaultEntryRepository = defaultEntryRepository;
    }

    public void addRepository(String key) {
        repositoryKeys.add(key);
    }

    public List<String> repositoryKeys() {
        return repositoryKeys;
    }
}
