package no.cantara.docsite;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import io.undertow.Undertow;
import no.cantara.docsite.cache.CacheInitializer;
import no.cantara.docsite.controller.ApplicationController;
import no.cantara.docsite.executor.ExecutorThreadPool;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.CacheManager;
import java.util.Map;

public class UndertowApplication {

    private static final Logger LOG = LoggerFactory.getLogger(UndertowApplication.class);

    public static String getDefaultConfigurationResourcePath() {
        return "application-defaults.properties";
    }

    public static UndertowApplication initializeUndertowApplication(DynamicConfiguration configuration) {
        LOG.info("Starting SourceCodePortal server");
        Map<String, String> configMap = configuration.asMap();
        configMap.put("github.password", "*****");
        configMap.put("github.oauth2.client.clientSecret", "*****");
        LOG.info("  Configuration: \n{}", configMap.toString().replaceAll(", ", ",\n     ")
                .replace("{", "{\n     ")
                .replace("}", "\n}"));
        int port = configuration.evaluateToInt("http.port");
        return initializeUndertowApplication(configuration, port);
    }

    public static UndertowApplication initializeUndertowApplication(DynamicConfiguration configuration, int port) {
        String host = configuration.evaluateToString("http.host");

        CacheManager cacheManager = CacheInitializer.initialize(configuration);

        ExecutorThreadPool executorThreadPool = new ExecutorThreadPool();

        ApplicationController applicationController = new ApplicationController(
                configuration.evaluateToString("http.cors.allow.origin"),
                configuration.evaluateToString("http.cors.allow.header"),
                configuration.evaluateToBoolean("http.cors.allow.origin.test"),
                port,
                cacheManager
        );

        return new UndertowApplication(configuration, host, port, executorThreadPool, cacheManager, applicationController);
    }

    private final DynamicConfiguration configuration;
    private final String host;
    private final int port;
    private final ExecutorThreadPool executorService;
    private final CacheManager cacheManager;
    private final Undertow server;

    public UndertowApplication(DynamicConfiguration configuration, String host, int port, ExecutorThreadPool executorService, CacheManager cacheManager, ApplicationController applicationController) {
        this.configuration = configuration;
        this.host = host;
        this.port = port;
        this.executorService = executorService;
        this.cacheManager = cacheManager;
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

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ExecutorThreadPool getExecutorService() {
        return executorService;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public Undertow getServer() {
        return server;
    }

    public void start() {
        server.start();
        LOG.info("Started Linked Data Store server. PID {}", ProcessHandle.current().pid());
        LOG.info("Listening on {}:{}", host, port);
    }

    public void stop() {
        executorService.shutdown();
        server.stop();
        cacheManager.close();
        LOG.info("Leaving.. Bye!");
    }

}
