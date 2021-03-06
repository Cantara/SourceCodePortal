package no.cantara.docsite.test.server;

import no.cantara.docsite.Application;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.executor.ExecutorService;
import no.ssb.config.DynamicConfiguration;

import java.net.MalformedURLException;
import java.net.URL;

public class TestServer implements TestUriResolver {

    final DynamicConfiguration configuration;
    final Application application;
    private final int testServerServicePort;

    public TestServer(DynamicConfiguration configuration, int testServerServicePort) {
        this.configuration = configuration;
        this.testServerServicePort = testServerServicePort;
        application = Application.initialize(configuration, testServerServicePort);
    }

    public void start() {
        application.start();
    }

    public void stop() {
        application.stop();
    }

    public String getTestServerHost() {
        return application.getHost();
    }

    public int getTestServerServicePort() {
        return testServerServicePort;
    }

    public CacheStore getCacheStore() {
        return application.getCacheStore();
    }

    public ExecutorService getExecutorService() {
        return application.getExecutorService();
    }

    public Application getApplication() {
        return application;
    }

    @Override
    public String testURL(String uri) {
        try {
            URL url = new URL("http", application.getHost(), application.getPort(), uri);
            return url.toExternalForm();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public DynamicConfiguration getConfiguration() {
        return configuration;
    }

}
