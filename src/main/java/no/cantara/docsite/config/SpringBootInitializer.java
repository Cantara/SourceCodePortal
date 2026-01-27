package no.cantara.docsite.config;

import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.config.RepositoryConfigLoader;
import no.cantara.docsite.executor.ExecutorService;
import no.cantara.docsite.fetch.PreFetchData;
import no.cantara.docsite.json.JsonbFactory;
import no.cantara.docsite.util.JavaUtilLoggerBridge;
import no.ssb.config.DynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.logging.Level;

/**
 * Spring Boot Application Initializer
 *
 * Handles initialization steps required for the Source Code Portal application.
 * This replaces the initialization logic from the legacy Server.java and Application.java.
 *
 * Initialization Steps:
 * 1. Install Java Util Logging bridge to SLF4J
 * 2. Load repository configuration from config.json
 * 3. Pre-fetch data if enabled (populate caches)
 *
 * Execution Order:
 * - Runs after Spring context is fully initialized
 * - Runs before @Scheduled tasks start
 * - Order priority: 1 (runs first if multiple ApplicationRunners exist)
 *
 * Note: This is only used when running in Spring Boot mode.
 * The legacy Undertow mode uses Server.java initialization.
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 8
 */
@Component
@Profile("!test")
@Order(1)
public class SpringBootInitializer implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SpringBootInitializer.class);

    private final ApplicationProperties properties;
    private final ConfigurationBridge configurationBridge;
    private final CacheStore cacheStore;
    private final ExecutorService executorService;

    public SpringBootInitializer(
        ApplicationProperties properties,
        ConfigurationBridge configurationBridge,
        CacheStore cacheStore,
        ExecutorService executorService
    ) {
        this.properties = properties;
        this.configurationBridge = configurationBridge;
        this.cacheStore = cacheStore;
        this.executorService = executorService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOG.info("=".repeat(80));
        LOG.info("Starting Source Code Portal Initialization");
        LOG.info("=".repeat(80));

        long startTime = System.currentTimeMillis();

        // Step 1: Install Java Util Logging bridge
        installLoggingBridge();

        // Step 2: Start executor service
        startExecutorService();

        // Step 3: Load repository configuration (only if prefetch enabled)
        if (properties.getCache().isPrefetch()) {
            loadRepositoryConfiguration();
        } else {
            LOG.info("Skipping repository configuration loading (prefetch disabled)");
        }

        // Step 4: Pre-fetch data if enabled
        preFetchData();

        long duration = System.currentTimeMillis() - startTime;
        LOG.info("=".repeat(80));
        LOG.info("Source Code Portal Initialization Complete in {}ms", duration);
        LOG.info("=".repeat(80));
        LOG.info("Server Mode: {}", properties.getServer().getMode());
        LOG.info("HTTP Port: {}", properties.getHttp().getPort());
        LOG.info("GitHub Organization: {}", properties.getGithub().getOrganization());
        LOG.info("Cache Enabled: {}", properties.getCache().isEnabled());
        LOG.info("Scheduling Enabled: {}", properties.getScheduled().isEnabled());
        LOG.info("=".repeat(80));
    }

    /**
     * Install Java Util Logging bridge to SLF4J
     *
     * This ensures all java.util.logging calls are routed through SLF4J/Logback.
     * Required for libraries that use JUL instead of SLF4J.
     */
    private void installLoggingBridge() {
        try {
            LOG.debug("Installing Java Util Logging bridge to SLF4J");
            JavaUtilLoggerBridge.installJavaUtilLoggerBridgeHandler(Level.INFO);
            LOG.debug("Java Util Logging bridge installed successfully");
        } catch (Exception e) {
            LOG.warn("Failed to install Java Util Logging bridge: {}", e.getMessage());
        }
    }

    /**
     * Start executor service
     *
     * The ExecutorService bean is created by ExecutorConfiguration but not started.
     * This starts the thread pool for async operations.
     */
    private void startExecutorService() {
        try {
            LOG.info("Starting executor service");
            executorService.start();
            LOG.info("Executor service started successfully");
        } catch (Exception e) {
            LOG.error("Failed to start executor service", e);
            throw new RuntimeException("Executor service initialization failed", e);
        }
    }

    /**
     * Load repository configuration from config.json
     *
     * This loads the repository group configuration and fetches the repository
     * list from GitHub. The results are cached for subsequent access.
     */
    private void loadRepositoryConfiguration() {
        try {
            LOG.info("Loading repository configuration");

            // Create DynamicConfiguration adapter for RepositoryConfigLoader
            DynamicConfiguration config = new DynamicConfigurationAdapter(configurationBridge);

            // Create and run config loader
            RepositoryConfigLoader configLoader = new RepositoryConfigLoader(config, cacheStore);
            configLoader.load();

            // Log configured repositories
            String configuredRepos = cacheStore.getConfiguredRepositories();
            LOG.info("Configured repositories loaded");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Configured repositories: {}", JsonbFactory.prettyPrint(configuredRepos));
            }

        } catch (Exception e) {
            LOG.error("Failed to load repository configuration", e);
            throw new RuntimeException("Repository configuration loading failed", e);
        }
    }

    /**
     * Pre-fetch data to populate caches
     *
     * If cache.prefetch is enabled, this fetches repository data, commits,
     * and other information from GitHub and caches it. This improves the
     * initial user experience by having data ready when the first request arrives.
     *
     * Pre-fetch is skipped if:
     * - cache.prefetch is false
     * - cacheStore already has repositories loaded
     */
    private void preFetchData() {
        try {
            boolean prefetchEnabled = properties.getCache().isPrefetch();

            if (!prefetchEnabled) {
                LOG.info("Pre-fetch disabled (cache.prefetch=false)");
                return;
            }

            // Check if repositories already loaded
            boolean hasRepositories = cacheStore.getRepositories().iterator().hasNext();
            if (!hasRepositories) {
                LOG.warn("No repositories loaded, cannot pre-fetch data");
                return;
            }

            LOG.info("Starting data pre-fetch");

            // Create DynamicConfiguration adapter for PreFetchData
            DynamicConfiguration config = new DynamicConfigurationAdapter(configurationBridge);

            // Create and run pre-fetch
            PreFetchData preFetchData = new PreFetchData(config, executorService, cacheStore);
            preFetchData.fetch();

            LOG.info("Data pre-fetch completed");

        } catch (Exception e) {
            LOG.error("Pre-fetch failed (non-fatal)", e);
            // Don't throw - pre-fetch failure shouldn't prevent startup
        }
    }
}
