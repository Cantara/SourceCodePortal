package no.cantara.docsite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot Application Entry Point
 *
 * This class serves as the modern Spring Boot entry point for the Source Code Portal.
 * It can run alongside the existing Server.java for gradual migration.
 *
 * Features enabled:
 * - Auto-configuration for web, caching, scheduling, actuator
 * - Component scanning in no.cantara.docsite package
 * - Caching with Caffeine
 * - Task scheduling
 *
 * To run:
 * - Via Maven: mvn spring-boot:run
 * - Via JAR: java -jar target/source-code-portal-*.jar
 * - Via IDE: Run this main() method
 *
 * Configuration:
 * - application.yml or application.properties
 * - Environment variables
 * - Command line arguments
 *
 * Migration Notes:
 * - This is Phase 2 of modernization (Spring Boot migration)
 * - Undertow server is used as embedded container (consistent with existing setup)
 * - Existing Server.java can still be used with --server.mode=undertow
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration
 */
@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.jsonb.JsonbAutoConfiguration.class
})
@EnableCaching
@EnableScheduling
public class SpringBootServer {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootServer.class, args);
    }
}
