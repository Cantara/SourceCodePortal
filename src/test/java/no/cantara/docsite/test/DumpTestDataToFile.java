package no.cantara.docsite.test;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import no.cantara.docsite.Application;
import no.cantara.docsite.util.CommonUtil;
import no.cantara.docsite.util.JavaUtilLoggerBridge;
import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DumpTestDataToFile {

    private static final Logger LOG = LoggerFactory.getLogger(DumpTestDataToFile.class);

    public static void main(String[] args) throws Exception {
        long now = System.currentTimeMillis();

        LoggerContext loggerContext = ((ch.qos.logback.classic.Logger) LOG).getLoggerContext();
        URL mainURL = ConfigurationWatchListUtil.getMainWatchURL(loggerContext);
        LOG.info("Logback used '{}' as the configuration file.", mainURL);

        DynamicConfiguration configuration = new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource(Application.getDefaultConfigurationResourcePath())
                .propertiesResource("application.properties")
                .propertiesResource("security.properties")
                .propertiesResource("application_override.properties")
                .values("cache.config", "conf/dump-cache-config.json")
                .values("http.hystrix.writeToFile", "true")
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
                while (application.getExecutorService().countActiveThreads() > 1 && application.getExecutorService().countRemainingWorkerTasks() > 0) {
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (InterruptedException e) {
            }
            TimeUnit.SECONDS.sleep(1);
            LOG.trace("Done fetching data!");
        } finally {
            application.stop();
            zip();
            System.exit(0);
        }
    }

    static void zip() throws IOException {
        Path dataPath = CommonUtil.getCurrentPath().resolve("target/data/");
        List<String> srcFiles = Files.walk(dataPath).filter(f -> f.toFile().isFile()).map(m -> m.toFile().toString().replace(dataPath.toString(), "").substring(1)).collect(Collectors.toList());
        Path targetFile = CommonUtil.getCurrentPath().resolve("src/test/resources/testdata.zipfile");
        Files.deleteIfExists(targetFile);
        FileOutputStream fos = new FileOutputStream(targetFile.toFile());
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (String srcFile : srcFiles) {
            File fileToZip = dataPath.resolve(srcFile).toFile();
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(srcFile);
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();
    }
}
