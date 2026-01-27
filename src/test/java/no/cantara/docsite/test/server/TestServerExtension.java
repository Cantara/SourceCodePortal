package no.cantara.docsite.test.server;

import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.test.ConfigurationOverride;
import no.cantara.docsite.test.ConfigurationProfile;
import no.cantara.docsite.test.client.TestClient;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class TestServerExtension implements BeforeEachCallback, AfterAllCallback, ParameterResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TestServerExtension.class);
    private static final Map<String, Long> timeStartProfilerMap = new ConcurrentHashMap<>(500);
    private static final Map<String, Long> timeStopProfilerMap = new ConcurrentHashMap<>(500);

    private static String shortName(final String testName) {
        int match = 2;
        int index = -1;
        String temp = testName;
        while (match != 0) {
            index = temp.lastIndexOf('.');
            match--;
            temp = temp.substring(0, index);
        }
        return testName.substring(index + 1);
    }

    private final ThreadLocal<DynamicConfiguration> configurationThreadLocal = new ThreadLocal<>();
    private final Map<DynamicConfiguration, TestServer> serverByConfiguration = new ConcurrentHashMap<>();
    private final Set<String> testclassHistory = new ConcurrentSkipListSet<>();
    private final ThreadLocal<String> currentTestClazz = new ThreadLocal<>();

    private final Map<String, DynamicConfiguration> configurationByProfile = new ConcurrentHashMap<>();

    public void addProfile(String profile, DynamicConfiguration configuration) {
        configurationByProfile.put(profile, configuration);
    }

    public DynamicConfiguration configurationInstance(String profile) {
        return configurationByProfile.get(profile);
    }

    public TestServerExtension() {
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        DynamicConfiguration configuration = configurationThreadLocal.get();
        if (configuration != null && configuration.evaluateToBoolean("test.time.profiler")) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n\n");
            timeStopProfilerMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new))
                    .forEach((k, v) -> {
                        sb.append(String.format("\t%s: %sms\n", shortName(k), v));
                    });
            if (LOG.isDebugEnabled()) LOG.debug("Test execution time profiling:{}", sb.toString());
        }
    }

    private TestServer startOrGetServer(DynamicConfiguration config) {
        return serverByConfiguration.computeIfAbsent(config, configuration -> {
            Random random = new Random();
            int testServerServicePort = (config.evaluateToString("http.port.test") != null ? config.evaluateToInt("http.port.test") : findFreePort(random, 9000, 9499));
            TestServer server = new TestServer(configuration, testServerServicePort);
            server.start();
            return server;
        });
    }

    public static int findFreePort(Random random, int from, int to) {
        int port = pick(random, from, to);
        for (int i = 0; i < 2 * ((to + 1) - from); i++) {
            if (isLocalPortFree(port)) {
                return port;
            }
            port = pick(random, from, to);
        }
        throw new IllegalStateException("Unable to find any available ports in range: [" + from + ", " + (to + 1) + ")");
    }

    private static int pick(Random random, int from, int to) {
        return from + random.nextInt((to + 1) - from);
    }

    private static boolean isLocalPortFree(int port) {
        try {
            new ServerSocket(port).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        String testClassName = context.getRequiredTestClass().getName();
        String testMethodName = context.getRequiredTestMethod().getName();

        LOG.trace("BEGIN {} # {}", context.getRequiredTestClass().getSimpleName(), testMethodName);

        boolean injectableTestClassFieldsInvalidated = false;
        if (testclassHistory.remove(testClassName)) {
            injectableTestClassFieldsInvalidated = true;
            LOG.info("Invalidating injected varaibles for: {}.{}", testClassName, testMethodName);
        }

        String previousTestClazz = currentTestClazz.get();
        if (!testClassName.equals(previousTestClazz)) {
            if (previousTestClazz != null) {
                testclassHistory.add(previousTestClazz);
            }
            currentTestClazz.set(testClassName);
        }

        ConfigurationProfile configurationProfile = ofNullable(context.getRequiredTestMethod())
                .map(m -> m.getDeclaredAnnotation(ConfigurationProfile.class)).orElse(null);
        ConfigurationOverride configurationOverride = ofNullable(context.getRequiredTestMethod())
                .map(m -> m.getDeclaredAnnotation(ConfigurationOverride.class)).orElse(null);

        String profile;
        if (configurationProfile != null) {
            profile = configurationProfile.value();
            StoreBasedDynamicConfiguration.Builder builder = new StoreBasedDynamicConfiguration.Builder()
                    .propertiesResource("application-defaults.properties")
                    .propertiesResource("application_test.properties")
                    .propertiesResource("security.properties")
                    .propertiesResource(String.format("application_test_%s.properties", profile));
            if (configurationOverride != null) {
                builder.values(configurationOverride.value());
            }
            addProfile(profile, builder.build());
        } else {
            profile = "TEST_DEFAULT";
            StoreBasedDynamicConfiguration.Builder builder = new StoreBasedDynamicConfiguration.Builder()
                    .propertiesResource("application-defaults.properties")
                    .propertiesResource("application_test.properties")
                    .propertiesResource("security.properties");
            if (configurationOverride != null) {
                builder.values(configurationOverride.value());
            }
            addProfile(profile, builder.build());
        }

        DynamicConfiguration configuration = configurationThreadLocal.get();
        if (configuration != null && !configuration.equals(configurationInstance(profile))) {
            // Configuration profile has changed, stop server to avoid using dirty configuration
            injectableTestClassFieldsInvalidated = true;
            LOG.debug("Configuration has changed, stopping server with dirty configuration...");
            TestServer server = serverByConfiguration.remove(configuration);
            if (server != null) {
                server.stop();
            }
        }

        configuration = configurationInstance(profile);
        configurationThreadLocal.set(configuration);

        if (configuration.evaluateToBoolean("test.time.profiler")) {
            final String test = testClassName + "." + testMethodName;
            timeStartProfilerMap.put(test, System.currentTimeMillis());
        }

        Object test = context.getRequiredTestInstance();
        Field[] fields = test.getClass().getDeclaredFields();
        for (Field field : fields) {
            // test server
            if (field.isAnnotationPresent(Inject.class) && TestServer.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    if (field.get(test) == null || injectableTestClassFieldsInvalidated) {
                        field.set(test, startOrGetServer(configuration));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            // test client
            if (field.isAnnotationPresent(Inject.class) && TestClient.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    if (field.get(test) == null || injectableTestClassFieldsInvalidated) {
                        field.set(test, TestClient.newClient(startOrGetServer(configuration)));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            // cache store
            if (field.isAnnotationPresent(Inject.class) && CacheStore.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    if (field.get(test) == null || injectableTestClassFieldsInvalidated) {
                        field.set(test, startOrGetServer(configuration).getCacheStore());
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // Store config in extension context for afterEach
        context.getStore(ExtensionContext.Namespace.create(getClass())).put("configuration", configuration);
        context.getStore(ExtensionContext.Namespace.create(getClass())).put("testName", testClassName + "." + testMethodName);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        return TestServer.class.equals(type) ||
               TestClient.class.equals(type) ||
               CacheStore.class.equals(type);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        DynamicConfiguration configuration = configurationThreadLocal.get();
        if (configuration == null) {
            throw new IllegalStateException("Configuration not initialized");
        }

        Class<?> type = parameterContext.getParameter().getType();
        if (TestServer.class.equals(type)) {
            return startOrGetServer(configuration);
        } else if (TestClient.class.equals(type)) {
            return TestClient.newClient(startOrGetServer(configuration));
        } else if (CacheStore.class.equals(type)) {
            return startOrGetServer(configuration).getCacheStore();
        }

        throw new IllegalStateException("Unsupported parameter type: " + type);
    }
}
