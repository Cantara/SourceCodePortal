package no.cantara.docsite.controller.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepoConfig;
import no.cantara.docsite.domain.jenkins.JenkinsBuildStatus;
import no.cantara.docsite.web.ResourceContext;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

public class BadgeResourceHandler implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BadgeResourceHandler.class);
    private final DynamicConfiguration configuration;
    private final CacheStore cacheStore;
    private final ResourceContext resourceContext;
    private final String resourcePath;

    public BadgeResourceHandler(DynamicConfiguration configuration, CacheStore cacheStore, ResourceContext resourceContext, String resourcePath) {
        this.configuration = configuration;
        this.cacheStore = cacheStore;
        this.resourceContext = resourceContext;
        this.resourcePath = resourcePath;
    }


    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        if (resourceContext.getTuples().size() > 2) {
            return;
        }

        String resourceName = resourceContext.getFirst().get().resource;
        String badgeCategory = resourceContext.getFirst().get().id;
        String repoName = resourceContext.getLast().get().resource;
        String branch = resourceContext.getLast().get().id;

        CacheKey cacheKey = CacheKey.of(cacheStore.getRepositoryConfig().getOrganization(RepoConfig.ScmProvider.GITHUB), repoName, branch);

        if ("jenkins".equalsIgnoreCase(badgeCategory)) {
            byte[] bytes;
            if (cacheStore.getJenkinsBuildStatus().containsKey(cacheKey)) {
                JenkinsBuildStatus buildStatus = cacheStore.getJenkinsBuildStatus().get(cacheKey);
                bytes = buildStatus.svg.getBytes();
            } else {
                URL url = ClassLoader.getSystemResource(String.format("%s/img/%s", resourcePath, "build-unknown.svg"));
                try (InputStream in = url.openStream()) {
                    bytes = in.readAllBytes();
                }
            }

            exchange.setStatusCode(200);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "image/svg+xml");
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            exchange.setResponseContentLength(bytes.length);
            exchange.getResponseSender().send(byteBuffer);
            return;
        }

        exchange.setStatusCode(404);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Not found: " + exchange.getRequestPath());
    }
}
