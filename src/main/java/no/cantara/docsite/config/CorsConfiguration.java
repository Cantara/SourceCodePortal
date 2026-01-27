package no.cantara.docsite.config;

import no.cantara.docsite.config.ApplicationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS Configuration for Spring Boot
 * <p>
 * Replaces the Undertow CORSController with Spring Boot's built-in CORS support.
 * <p>
 * Configuration is loaded from application.yml (scp.http.cors.*)
 */
@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    private final ApplicationProperties properties;

    public CorsConfiguration(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String allowOrigin = properties.getHttp().getCors().getAllowOrigin();
        String allowHeader = properties.getHttp().getCors().getAllowHeader();

        registry.addMapping("/**")  // Apply to all endpoints
                .allowedOrigins(parseAllowedOrigins(allowOrigin))
                .allowedMethods("GET", "PUT", "DELETE", "OPTIONS", "HEAD", "POST")
                .allowedHeaders(allowHeader)
                .allowCredentials(false);
    }

    /**
     * Parse allowed origins from configuration.
     * Supports "*" (all origins) or comma-separated list of origins.
     */
    private String[] parseAllowedOrigins(String allowOrigin) {
        if ("*".equals(allowOrigin)) {
            return new String[]{"*"};
        }
        // Split comma-separated origins
        return allowOrigin.split(",");
    }
}
