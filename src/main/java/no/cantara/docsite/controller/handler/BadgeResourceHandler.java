package no.cantara.docsite.controller.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepoConfig;
import no.cantara.docsite.domain.jenkins.JenkinsBuildStatus;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.cantara.docsite.domain.scm.ScmRepositoryService;
import no.cantara.docsite.domain.shields.ShieldsStatus;
import no.cantara.docsite.domain.snyk.SnykTestStatus;
import no.cantara.docsite.web.ResourceContext;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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


    private byte[] readBadge(String badgeResourceName) throws IOException {
        byte[] bytes;
        URL url = ClassLoader.getSystemResource(String.format("%s/img/%s", resourcePath, badgeResourceName));
        try (InputStream in = url.openStream()) {
            bytes = in.readAllBytes();
        }
        return bytes;
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

        CacheKey cacheKey = CacheKey.of(cacheStore.getOldRepositoryConfig().getOrganization(RepoConfig.ScmProvider.GITHUB), repoName, branch);

        if ("license".equalsIgnoreCase(badgeCategory)) {
            byte[] bytes;
            ScmRepository scmRepository = new ScmRepositoryService(cacheStore).getFirst(cacheKey);
            if (scmRepository == null || (scmRepository != null && scmRepository.licenseURL.getLicenseSpdxId() == null)) {
                bytes = readBadge("license-invalid.svg");
            } else {
                String svg = new String(readBadge("license-asl2.svg"));
                if (!"Apache-2.0".equalsIgnoreCase(scmRepository.licenseURL.getLicenseSpdxId())) {
                    svg = svg.replace("Apache 2", scmRepository.licenseURL.getLicenseSpdxId());
                }
                bytes = svg.getBytes();
            }
            exchange.setStatusCode(200);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "image/svg+xml");
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            exchange.setResponseContentLength(bytes.length);
            exchange.getResponseSender().send(byteBuffer);
            return;
        }

        if ("jenkins".equalsIgnoreCase(badgeCategory)) {
            byte[] bytes;
            if (cacheStore.getJenkinsBuildStatus().containsKey(cacheKey)) {
                JenkinsBuildStatus buildStatus = cacheStore.getJenkinsBuildStatus().get(cacheKey);
                bytes = buildStatus.svg.getBytes();
            } else {
                bytes = readBadge("build-unknown.svg");
            }

            exchange.setStatusCode(200);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "image/svg+xml");
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            exchange.setResponseContentLength(bytes.length);
            exchange.getResponseSender().send(byteBuffer);
            return;
        }

        if ("snyk".equalsIgnoreCase(badgeCategory)) {
            byte[] bytes;
            if (cacheStore.getSnykTestStatus().containsKey(cacheKey)) {
                SnykTestStatus snykTestStatus = cacheStore.getSnykTestStatus().get(cacheKey);
                bytes = snykTestStatus.svg.getBytes();
            } else {
                bytes = readBadge("snyk-unknown-lightgrey.svg");
            }

            exchange.setStatusCode(200);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "image/svg+xml");
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            exchange.setResponseContentLength(bytes.length);
            exchange.getResponseSender().send(byteBuffer);
            return;
        }

        if ("shields-issues".equalsIgnoreCase(badgeCategory) || "shields-commits".equalsIgnoreCase(badgeCategory) || "shields-releases".equalsIgnoreCase(badgeCategory)) {
            byte[] bytes = new byte[0];
            if ("shields-issues".equalsIgnoreCase(badgeCategory)) {
                ShieldsStatus shieldsStatus = cacheStore.getSheildIssuesStatus().get(cacheKey);
                bytes = (shieldsStatus == null ? readBadge("issues-zero.svg") : shieldsStatus.svg.getBytes());

            } else if ("shields-commits".equalsIgnoreCase(badgeCategory)) {
                ShieldsStatus shieldsStatus = cacheStore.getSheildCommitsStatus().get(cacheKey);
                bytes = (shieldsStatus == null ? readBadge("commit-unknown.svg") : shieldsStatus.svg.getBytes());

            } else if ("shields-releases".equalsIgnoreCase(badgeCategory)) {
                ShieldsStatus shieldsStatus = cacheStore.getShieldReleasesStatus().get(cacheKey);;
                bytes = (shieldsStatus == null ? readBadge("release-unknown.svg") : shieldsStatus.svg.getBytes());
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
