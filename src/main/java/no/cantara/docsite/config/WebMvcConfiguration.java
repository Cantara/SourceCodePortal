package no.cantara.docsite.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC Configuration
 *
 * Configures Spring MVC settings including CORS, view resolution, and resource handling.
 *
 * CORS Configuration:
 * - Replaces the custom CORSController from Undertow setup
 * - Uses Spring's built-in CORS support
 * - Configuration from ApplicationProperties
 *
 * View Resolution:
 * - Thymeleaf configured via application.yml
 * - Template location: classpath:/META-INF/views/
 * - File suffix: .html
 *
 * Static Resources:
 * - CSS: /css/** → classpath:/META-INF/views/css/
 * - JS: /js/** → classpath:/META-INF/views/js/
 * - Images: /img/** → classpath:/META-INF/views/img/
 * - Automatically handled by Spring Boot
 *
 * Migration from Undertow:
 * Before (Undertow):
 * - Custom CORSController with manual header setting
 * - Manual resource handling with StaticContentController
 * - Custom routing in ApplicationController
 *
 * After (Spring MVC):
 * - Declarative CORS with @CrossOrigin or WebMvcConfigurer
 * - Automatic static resource handling
 * - Convention-based routing with @Controller/@RestController
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 5
 */
@Configuration
@Profile("xxx-disabled")
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final ApplicationProperties properties;

    public WebMvcConfiguration(ApplicationProperties properties) {
        this.properties = properties;
    }

    /**
     * Configure CORS mappings
     *
     * Replaces custom CORSController logic:
     * - Allow origin from configuration
     * - Allow headers from configuration
     * - Allow all HTTP methods
     * - Allow credentials if needed
     *
     * Configuration:
     * - scp.http.cors.allow-origin: Allowed origins (default: *)
     * - scp.http.cors.allow-header: Allowed headers (default: Content-Type)
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String allowOrigin = properties.getHttp().getCors().getAllowOrigin();
        String allowHeader = properties.getHttp().getCors().getAllowHeader();

        registry.addMapping("/**")
            .allowedOrigins(allowOrigin)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders(allowHeader, "Accept", "Authorization")
            .allowCredentials(false)
            .maxAge(3600);
    }

    /**
     * Additional configuration can be added here:
     *
     * - addViewControllers(): Simple redirects without controller logic
     * - addResourceHandlers(): Custom static resource handling
     * - addInterceptors(): Request/response interceptors
     * - configureContentNegotiation(): Content type negotiation
     * - addFormatters(): Custom data converters
     */

    /*
    Example: Add view controllers for simple redirects
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/dashboard");
        registry.addViewController("/login").setViewName("login");
    }
    */

    /*
    Example: Custom resource handling (if needed)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
            .addResourceLocations("classpath:/META-INF/views/css/")
            .setCachePeriod(3600);

        registry.addResourceHandler("/js/**")
            .addResourceLocations("classpath:/META-INF/views/js/")
            .setCachePeriod(3600);

        registry.addResourceHandler("/img/**", "/favicon.ico")
            .addResourceLocations("classpath:/META-INF/views/img/")
            .setCachePeriod(86400);
    }
    */
}
