package no.cantara.docsite;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import no.cantara.docsite.util.JavaUtilLoggerBridge;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.logging.Level;

import static org.jsoup.helper.Validate.fail;

/**
 * TODO refactor the webapp to its own web project after project split between core, thymeleaf web, etc.
 */
public class Server {

    static {
        try {
            if (Cipher.getMaxAllowedKeyLength("AES") < 256) {
                Field field = Class.forName("javax.crypto.JceSecurity").
                        getDeclaredField("isRestricted");
                field.setAccessible(true);
                field.set(null, java.lang.Boolean.FALSE);
            }
        } catch (Exception e) {
            fail("Could not override JCE cryptography strength policy setting");
            fail(e.getMessage());
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        long now = System.currentTimeMillis();

        LoggerContext loggerContext = ((ch.qos.logback.classic.Logger) LOG).getLoggerContext();
        URL mainURL = ConfigurationWatchListUtil.getMainWatchURL(loggerContext);
        LOG.info("Logback used '{}' as the configuration file.", mainURL);

        DynamicConfiguration configuration = new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource(Application.getDefaultConfigurationResourcePath())
                .propertiesResource("application.properties")
                .propertiesResource("security.properties")
                .propertiesResource("application_override.properties")
                .environment("SCP_")
                .systemProperties()
                .build();

        JavaUtilLoggerBridge.installJavaUtilLoggerBridgeHandler(Level.INFO);

        Application application = Application.initialize(configuration);

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOG.warn("ShutdownHook triggered..");
                application.stop();
            }));

            application.enableCacheManager();

            application.enableExecutorService();

            application.enableScheduledExecutorService();

            application.enableConfigLoader();

            application.enablePreFetch();

            application.start();

            long time = System.currentTimeMillis() - now;
            LOG.info("Server started in {}ms..", time);

            // wait for termination signal
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
            }
        } finally {
            application.stop();
        }
    }
}
