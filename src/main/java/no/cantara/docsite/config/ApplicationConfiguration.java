package no.cantara.docsite.config;

import no.ssb.config.DynamicConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * General Application Configuration
 *
 * <p>Provides general-purpose Spring beans for the application.
 *
 * <h2>Beans Provided</h2>
 * <ul>
 *   <li>{@link DynamicConfiguration} - Legacy configuration adapter for backward compatibility</li>
 * </ul>
 *
 * @since Week 2-3 Controller Migration (fix for missing DynamicConfiguration bean)
 */
@Configuration
public class ApplicationConfiguration {

    /**
     * Creates DynamicConfiguration bean for legacy code compatibility
     *
     * <p>Many legacy components (PreFetchData, RepositoryConfigLoader, controllers, etc.)
     * expect DynamicConfiguration to be available. This bean wraps the Spring Boot
     * ConfigurationBridge with a DynamicConfiguration adapter to provide backward compatibility.
     *
     * <p>Controllers and services that need configuration should inject this bean:
     * <pre>
     * public MyController(DynamicConfiguration configuration) {
     *     this.configuration = configuration;
     * }
     * </pre>
     *
     * @param configurationBridge Spring Boot configuration bridge
     * @return DynamicConfiguration adapter wrapping the bridge
     */
    @Bean
    public DynamicConfiguration dynamicConfiguration(ConfigurationBridge configurationBridge) {
        return new DynamicConfigurationAdapter(configurationBridge);
    }
}
