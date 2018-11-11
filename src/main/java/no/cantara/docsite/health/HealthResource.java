package no.cantara.docsite.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class HealthResource {

    private static final Logger LOG = LoggerFactory.getLogger(HealthResource.class);
    private static final String DEFAULT_VERSION = "(DEV VERSION)";

    private static final class HealthHolder {
        private static final HealthResource instance = new HealthResource();
    }

    public static HealthResource instance() {
        return HealthHolder.instance;
    }

    private final AtomicLong gitHubLastSeen = new AtomicLong(0);
    private final AtomicLong cantaraWikiLastSeen = new AtomicLong(0);
    private final AtomicLong jenkinsLastSeen = new AtomicLong(0);
    private final AtomicLong snykLastSeen = new AtomicLong(0);
    private final AtomicLong shieldsLastSeen = new AtomicLong(0);
    private final Map<String,Long> scheduledWorkersLastSeen = new ConcurrentHashMap<>();

    public void markGitHubLastSeen() {
        gitHubLastSeen.set(System.currentTimeMillis());
    }

    public long getGitHubLastSeen() {
        return gitHubLastSeen.get();
    }

    public void markCantaraWikiLastSeen() {
        cantaraWikiLastSeen.set(System.currentTimeMillis());
    }

    public long getCantaraWikiLastSeen() {
        return cantaraWikiLastSeen.get();
    }

    public void markJenkinsLastSeen() {
        jenkinsLastSeen.set(System.currentTimeMillis());
    }

    public long getJenkinLastSeen() {
        return jenkinsLastSeen.get();
    }

    public void markSnykLastSeen() {
        snykLastSeen.set(System.currentTimeMillis());
    }

    public long getSnykLastSeen() {
        return snykLastSeen.get();
    }

    public void markShieldsLastSeen() {
        shieldsLastSeen.set(System.currentTimeMillis());
    }

    public long getShieldsLastSeen() {
        return shieldsLastSeen.get();
    }

    public void markScheduledWorkerLastSeen(String id) {
        scheduledWorkersLastSeen.put(id, System.currentTimeMillis());
    }

    public Long getScheduledWorkerLastSeen(String id) {
        return scheduledWorkersLastSeen.put(id, System.currentTimeMillis());
    }

    public JsonObjectBuilder getThreadInfo() {
        Set<Thread> threads = Thread.getAllStackTraces().keySet();

        JsonObjectBuilder builder = Json.createObjectBuilder();

        for (Thread t : threads) {
            String name = t.getName();
            Thread.State state = t.getState();
            int priority = t.getPriority();
            String type = t.isDaemon() ? "Daemon" : "Normal";
            JsonObjectBuilder threadBuilder = Json.createObjectBuilder();
            threadBuilder.add("id", t.getId());
            threadBuilder.add("state", state.toString());
            threadBuilder.add("priority", priority);
            threadBuilder.add("alive", t.isAlive());
            threadBuilder.add("interrupted", t.isInterrupted());
            threadBuilder.add("type", type);
            builder.add(name, threadBuilder);
        }

        return builder;
    }

    public String getRunningSince() {
        long uptimeInMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        return Instant.now().minus(uptimeInMillis, ChronoUnit.MILLIS).toString();
    }

    public String getVersion() {
        Properties mavenProperties = new Properties();
        String resourcePath = "/META-INF/maven/no.cantara.docsite/source-code-portal/pom.properties";
        URL mavenVersionResource = this.getClass().getResource(resourcePath);
        if (mavenVersionResource != null) {
            try {
                mavenProperties.load(mavenVersionResource.openStream());
                return mavenProperties.getProperty("version", "missing version info in " + resourcePath);
            } catch (IOException e) {
                LOG.warn("Problem reading version resource from classpath: ", e);
            }
        }
        return DEFAULT_VERSION;
    }

    public boolean isDevelopment() {
        return DEFAULT_VERSION.equalsIgnoreCase(getVersion());
    }

}
