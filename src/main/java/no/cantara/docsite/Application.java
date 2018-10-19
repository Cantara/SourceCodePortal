package no.cantara.docsite;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import io.undertow.Undertow;
import no.cantara.docsite.cache.CacheInitializer;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.controller.ApplicationController;
import no.cantara.docsite.domain.config.RepositoryConfigLoader;
import no.cantara.docsite.domain.github.contents.PreFetchRepositoryContents;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.health.HealthResource;
import no.cantara.docsite.util.JsonUtil;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static String getDefaultConfigurationResourcePath() {
        return "application-defaults.properties";
    }

    public static Application initialize(DynamicConfiguration configuration) {
        LOG.info("Starting SourceCodePortal server");
        if (!HealthResource.instance().isDevelopment()) {
            Map<String, String> configMap = configuration.asMap();
            configMap.put("github.password", "*****");
            configMap.put("github.oauth2.client.clientSecret", "*****");
            configMap.put("github.client.accessToken", "*****");
            LOG.info("  Configuration: \n{}", configMap.toString().replaceAll(", ", ",\n     ")
                    .replace("{", "{\n     ")
                    .replace("}", "\n}"));
        }
        int port = configuration.evaluateToInt("http.port");
        return initialize(configuration, port);
    }

    public static Application initialize(DynamicConfiguration configuration, int port) {
        String host = configuration.evaluateToString("http.host");

        CacheStore cacheStore = CacheInitializer.initialize(configuration);

        ExecutorService executorService = ExecutorService.create();

        RepositoryConfigLoader configLoader = new RepositoryConfigLoader(configuration, cacheStore);

        ApplicationController applicationController = new ApplicationController(
                configuration.evaluateToString("http.cors.allow.origin"),
                configuration.evaluateToString("http.cors.allow.header"),
                configuration.evaluateToBoolean("http.cors.allow.origin.test"),
                port,
                configuration,
                executorService,
                cacheStore
        );

        return new Application(configuration, host, port, executorService, cacheStore, configLoader, applicationController);
    }

    private final DynamicConfiguration configuration;
    private final String host;
    private final int port;
    private final ExecutorService executorService;
    private final CacheStore cacheStore;
    private final RepositoryConfigLoader configLoader;
    private final Undertow server;

    public Application(DynamicConfiguration configuration, String host, int port, ExecutorService executorService, CacheStore cacheStore, RepositoryConfigLoader configLoader, ApplicationController applicationController) {
        this.configuration = configuration;
        this.host = host;
        this.port = port;
        this.executorService = executorService;
        this.cacheStore = cacheStore;
        this.configLoader = configLoader;
        HystrixCommandProperties.Setter()
                .withExecutionTimeoutInMilliseconds(2500)
                .withExecutionIsolationSemaphoreMaxConcurrentRequests(25);
        HystrixThreadPoolProperties.Setter()
                .withMaxQueueSize(100)
                .withQueueSizeRejectionThreshold(100)
                .withCoreSize(4);
        this.server = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(applicationController)
                .build();
    }

    public void enableExecutorService() {
        executorService.start();
    }

    public void enableCacheManager() {
        // TODO create default caches
    }

    public void enableConfigLoader() {
        configLoader.load();
        LOG.info("Configured repositories:{}", JsonUtil.prettyPrint(cacheStore.getConfiguredRepositories()));
    }

    public void enablePreFetch() {
        if (!cacheStore.getRepositoryGroups().iterator().hasNext()) {
            enableConfigLoader();
        }
        if (configuration.evaluateToBoolean("cache.prefetch")) {
            PreFetchRepositoryContents preFetchRepositoryContents = new PreFetchRepositoryContents(configuration, executorService, cacheStore);
            preFetchRepositoryContents.fetch();
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public CacheStore getCacheStore() {
        return cacheStore;
    }

    public Undertow getServer() {
        return server;
    }

    public void start() {
        server.start();
        LOG.info("Started SourceCodePortal server. PID {}", ProcessHandle.current().pid());
        LOG.info("Listening on {}:{}", host, port);
    }

    public void stop() {
        executorService.shutdown();
        server.stop();
        cacheStore.getCacheManager().close();
        LOG.info("Leaving.. Bye!");
    }

}
