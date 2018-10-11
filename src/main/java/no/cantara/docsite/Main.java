package no.cantara.docsite;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import io.undertow.Undertow;
import no.cantara.docsite.controller.ApplicationController;
import no.cantara.docsite.executor.ExecutorThreadPool;
import no.cantara.docsite.util.JavaUtilLoggerBridge;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.logging.Level;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private final DynamicConfiguration configuration;
    private Undertow server;
    private final String host;
    private final int port;
    private ExecutorThreadPool worker;

    public Main(DynamicConfiguration configuration, String host, int port) {
        LOG.info("Starting SourceCodePortal server");
        LOG.info("  Configuration: {}", configuration.asMap().toString().replaceAll(", ", ",\n     ").replace("{", "{\n     "));
        this.configuration = configuration;
        this.host = host;
        this.port = port;
        HystrixCommandProperties.Setter()
                .withExecutionTimeoutInMilliseconds(2500)
                .withExecutionIsolationSemaphoreMaxConcurrentRequests(25);
        HystrixThreadPoolProperties.Setter()
                .withMaxQueueSize(100)
                .withQueueSizeRejectionThreshold(100)
                .withCoreSize(4);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public synchronized void start() {
        if (server == null) {
            worker = new ExecutorThreadPool();
            worker.start();

            ApplicationController handler = new ApplicationController(
                    configuration.evaluateToString("http.cors.allow.origin"),
                    configuration.evaluateToString("http.cors.allow.header"),
                    configuration.evaluateToBoolean("http.cors.allow.origin.test"),
                    port
            );

            server = Undertow.builder()
                    .addHttpListener(port, host)
                    .setHandler(handler)
                    .build();

            server.start();

            LOG.info("Listening on {}:{}", host, port);
        }
    }

    public synchronized void stop() {
        if (server != null) {
            worker.shutdown();
            server.stop();
            server = null;
            LOG.info("Leaving.. Bye!");
        }
    }

    public synchronized Undertow getUndertowServer() {
        return server;
    }

    public static void main(String[] args) {
        long now = System.currentTimeMillis();

        LoggerContext loggerContext = ((ch.qos.logback.classic.Logger) LOG).getLoggerContext();
        URL mainURL = ConfigurationWatchListUtil.getMainWatchURL(loggerContext);
        LOG.info("Logback used '{}' as the configuration file.", mainURL);

        DynamicConfiguration configuration = new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource("application-defaults.properties")
                .propertiesResource("application.properties")
                .propertiesResource("security.properties")
                .propertiesResource("application_override.properties")
                .environment("SCP_")
                .systemProperties()
                .build();

        JavaUtilLoggerBridge.installJavaUtilLoggerBridgeHandler(Level.INFO);

        String host = configuration.evaluateToString("http.host");
        int port = configuration.evaluateToInt("http.port");
        Main main = new Main(configuration, host, port);
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOG.warn("ShutdownHook triggered..");
                main.stop();
            }));
            main.start();

            long time = System.currentTimeMillis() - now;
            LOG.info("Server started in {}ms..", time);

            // wait for termination signal
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
            }
        } finally {
            main.stop();
        }
    }

}
