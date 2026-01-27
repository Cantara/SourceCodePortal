package no.cantara.docsite.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Spring Boot Configuration Properties for Source Code Portal
 *
 * Replaces DynamicConfiguration with type-safe Spring Boot properties.
 * Maps to application.yml with prefix "scp"
 *
 * Usage:
 * <pre>
 * @Autowired
 * private ApplicationProperties config;
 * String org = config.getGithub().getOrganization();
 * </pre>
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration
 */
@Configuration
@ConfigurationProperties(prefix = "scp")
public class ApplicationProperties {

    private Server server = new Server();
    private Http http = new Http();
    private Cache cache = new Cache();
    private GitHub github = new GitHub();
    private Render render = new Render();
    private Scheduled scheduled = new Scheduled();
    private Jenkins jenkins = new Jenkins();
    private Snyk snyk = new Snyk();
    private Shields shields = new Shields();

    // Getters and Setters

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Http getHttp() {
        return http;
    }

    public void setHttp(Http http) {
        this.http = http;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public GitHub getGithub() {
        return github;
    }

    public void setGithub(GitHub github) {
        this.github = github;
    }

    public Render getRender() {
        return render;
    }

    public void setRender(Render render) {
        this.render = render;
    }

    public Scheduled getScheduled() {
        return scheduled;
    }

    public void setScheduled(Scheduled scheduled) {
        this.scheduled = scheduled;
    }

    public Jenkins getJenkins() {
        return jenkins;
    }

    public void setJenkins(Jenkins jenkins) {
        this.jenkins = jenkins;
    }

    public Snyk getSnyk() {
        return snyk;
    }

    public void setSnyk(Snyk snyk) {
        this.snyk = snyk;
    }

    public Shields getShields() {
        return shields;
    }

    public void setShields(Shields shields) {
        this.shields = shields;
    }

    /**
     * Server configuration
     */
    public static class Server {
        private String mode = "spring-boot"; // or "undertow"

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }

    /**
     * HTTP server configuration
     */
    public static class Http {
        private String host = "0.0.0.0";
        private int port = 9090;
        private Cors cors = new Cors();

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public Cors getCors() {
            return cors;
        }

        public void setCors(Cors cors) {
            this.cors = cors;
        }

        public static class Cors {
            private String allowOrigin = "*";
            private String allowHeader = "Content-Type";
            private boolean allowOriginTest = false;

            public String getAllowOrigin() {
                return allowOrigin;
            }

            public void setAllowOrigin(String allowOrigin) {
                this.allowOrigin = allowOrigin;
            }

            public String getAllowHeader() {
                return allowHeader;
            }

            public void setAllowHeader(String allowHeader) {
                this.allowHeader = allowHeader;
            }

            public boolean isAllowOriginTest() {
                return allowOriginTest;
            }

            public void setAllowOriginTest(boolean allowOriginTest) {
                this.allowOriginTest = allowOriginTest;
            }
        }
    }

    /**
     * Cache configuration
     */
    public static class Cache {
        private String config = "conf/config.json";
        private boolean prefetch = true;
        private boolean management = false;
        private boolean statistics = false;
        private boolean enabled = true;
        private int ttlMinutes = 30;

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        public boolean isPrefetch() {
            return prefetch;
        }

        public void setPrefetch(boolean prefetch) {
            this.prefetch = prefetch;
        }

        public boolean isManagement() {
            return management;
        }

        public void setManagement(boolean management) {
            this.management = management;
        }

        public boolean isStatistics() {
            return statistics;
        }

        public void setStatistics(boolean statistics) {
            this.statistics = statistics;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getTtlMinutes() {
            return ttlMinutes;
        }

        public void setTtlMinutes(int ttlMinutes) {
            this.ttlMinutes = ttlMinutes;
        }
    }

    /**
     * GitHub configuration
     */
    public static class GitHub {
        private String organization = "Cantara";
        private String clientId;
        private String clientSecret;
        private String accessToken;
        private Repository repository = new Repository();
        private Webhook webhook = new Webhook();

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public Repository getRepository() {
            return repository;
        }

        public void setRepository(Repository repository) {
            this.repository = repository;
        }

        public Webhook getWebhook() {
            return webhook;
        }

        public void setWebhook(Webhook webhook) {
            this.webhook = webhook;
        }

        public static class Repository {
            private String visibility = "public"; // public, private, all

            public String getVisibility() {
                return visibility;
            }

            public void setVisibility(String visibility) {
                this.visibility = visibility;
            }
        }

        public static class Webhook {
            private String securityAccessToken;

            public String getSecurityAccessToken() {
                return securityAccessToken;
            }

            public void setSecurityAccessToken(String securityAccessToken) {
                this.securityAccessToken = securityAccessToken;
            }
        }
    }

    /**
     * Rendering configuration
     */
    public static class Render {
        private int maxGroupCommits = 5;

        public int getMaxGroupCommits() {
            return maxGroupCommits;
        }

        public void setMaxGroupCommits(int maxGroupCommits) {
            this.maxGroupCommits = maxGroupCommits;
        }
    }

    /**
     * Scheduled tasks configuration
     */
    public static class Scheduled {
        private boolean enabled = true;
        private Duration tasksInterval = Duration.ofSeconds(60);
        private int repositoryRefreshMinutes = 30;
        private int commitFetchMinutes = 15;
        private Jenkins jenkins = new Jenkins();
        private Snyk snyk = new Snyk();
        private Shields shields = new Shields();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getTasksInterval() {
            return tasksInterval;
        }

        public void setTasksInterval(Duration tasksInterval) {
            this.tasksInterval = tasksInterval;
        }

        public int getRepositoryRefreshMinutes() {
            return repositoryRefreshMinutes;
        }

        public void setRepositoryRefreshMinutes(int repositoryRefreshMinutes) {
            this.repositoryRefreshMinutes = repositoryRefreshMinutes;
        }

        public int getCommitFetchMinutes() {
            return commitFetchMinutes;
        }

        public void setCommitFetchMinutes(int commitFetchMinutes) {
            this.commitFetchMinutes = commitFetchMinutes;
        }

        public Jenkins getJenkins() {
            return jenkins;
        }

        public void setJenkins(Jenkins jenkins) {
            this.jenkins = jenkins;
        }

        public Snyk getSnyk() {
            return snyk;
        }

        public void setSnyk(Snyk snyk) {
            this.snyk = snyk;
        }

        public Shields getShields() {
            return shields;
        }

        public void setShields(Shields shields) {
            this.shields = shields;
        }

        public static class Jenkins {
            private int intervalMinutes = 5;

            public int getIntervalMinutes() {
                return intervalMinutes;
            }

            public void setIntervalMinutes(int intervalMinutes) {
                this.intervalMinutes = intervalMinutes;
            }
        }

        public static class Snyk {
            private int intervalMinutes = 15;

            public int getIntervalMinutes() {
                return intervalMinutes;
            }

            public void setIntervalMinutes(int intervalMinutes) {
                this.intervalMinutes = intervalMinutes;
            }
        }

        public static class Shields {
            private int intervalMinutes = 15;

            public int getIntervalMinutes() {
                return intervalMinutes;
            }

            public void setIntervalMinutes(int intervalMinutes) {
                this.intervalMinutes = intervalMinutes;
            }
        }
    }

    /**
     * Jenkins integration configuration
     */
    public static class Jenkins {
        private String baseUrl = "https://jenkins.quadim.ai";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    /**
     * Snyk integration configuration
     */
    public static class Snyk {
        private String apiToken;
        private String organizationId;

        public String getApiToken() {
            return apiToken;
        }

        public void setApiToken(String apiToken) {
            this.apiToken = apiToken;
        }

        public String getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(String organizationId) {
            this.organizationId = organizationId;
        }
    }

    /**
     * Shields.io integration configuration
     */
    public static class Shields {
        private String baseUrl = "https://img.shields.io";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
