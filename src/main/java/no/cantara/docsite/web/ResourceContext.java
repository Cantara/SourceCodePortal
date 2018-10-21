package no.cantara.docsite.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResourceContext {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceContext.class);
    private final List<Resource> tuples = new ArrayList<>();

    public ResourceContext(String requestPath) {
        List<String> elements = Arrays.asList(requestPath.split("/")).stream().filter(f -> !"".equals(f)).collect(Collectors.toList());
        Resource resource = null;
        for (int n = 0; n < elements.size(); ) {
            resource = new Resource(this, resource, elements.get(n), (elements.size() > n + 1 ? elements.get(n + 1) : null));
            tuples.add(resource);
            n = n + 2;
        }
    }

    public List<Resource> getTuples() {
        return tuples;
    }

    public Optional<Resource> getFirst() {
        return Optional.ofNullable((tuples.size() > 0 ? tuples.get(0) : null));
    }

    public Optional<Resource> getLast() {
        return Optional.ofNullable((tuples.size() > 0 ? tuples.get(tuples.size() - 1) : null));
    }

    public static class Resource {
        private final ResourceContext resourceContext;
        public final Resource parent;
        public final String resource;
        public final String id;

        public Resource(ResourceContext resourceContext, Resource parent, String resource, String id) {
            this.resourceContext = resourceContext;
            this.parent = parent;
            this.resource = resource;
            this.id = id;
        }

        public String getPath() {
            Resource current = this;
            StringBuilder builder = new StringBuilder();
            while (current != null) {
                builder.insert(0, "/" + current.resource);
                current = current.parent;
            }
            return builder.toString();
        }

        public String getExactPath() {
            Resource current = this;
            StringBuilder builder = new StringBuilder();
            while (current != null) {
                builder.insert(0, "/" + current.resource + "/" + current.id);
                current = current.parent;
            }
            return builder.toString();
        }

        public boolean match(String resourcePath) {
            ResourceContext thatResourceContext = new ResourceContext(resourcePath);
            return getPath().equals(thatResourceContext.getLast().get().getPath());
        }

        public boolean subMatch(String resourcePath) {
            ResourceContext thatResourceContext = new ResourceContext(resourcePath);
            for(int n = 0; n < thatResourceContext.tuples.size(); n++) {
                Resource thisResource = resourceContext.tuples.get(n);
                Resource thatResource = thatResourceContext.tuples.get(n);
                if (thisResource.resource.equals(thatResource.resource)) {
                    return true;
                }
            }
            return false;
        }

        public boolean exactMatch(String resourcePath) {
            ResourceContext thatResourceContext = new ResourceContext(resourcePath);
            return getExactPath().equals(thatResourceContext.getLast().get().getExactPath());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Resource)) return false;
            Resource resource1 = (Resource) o;
            return Objects.equals(parent, resource1.parent) &&
                    Objects.equals(resource, resource1.resource) &&
                    Objects.equals(id, resource1.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parent, resource, id);
        }

        @Override
        public String toString() {
            return "Resource{" +
                    "parent=" + parent +
                    ", resource='" + resource + '\'' +
                    ", id='" + id + '\'' +
                    '}';
        }

    }
}
