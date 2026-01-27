package no.cantara.docsite.controller.spring;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring MVC Ping Controller
 *
 * Replaces the Undertow-based PingController with a Spring MVC @RestController.
 *
 * Endpoints:
 * - GET /ping: Simple health check that returns HTTP 200 OK
 *
 * Migration from Undertow:
 * Before (Undertow):
 * <pre>
 * class PingController implements HttpHandler {
 *     public void handleRequest(HttpServerExchange exchange) {
 *         exchange.setStatusCode(HttpURLConnection.HTTP_OK);
 *     }
 * }
 * </pre>
 *
 * After (Spring MVC):
 * <pre>
 * @RestController
 * public class PingRestController {
 *     @GetMapping("/ping")
 *     public ResponseEntity<Void> ping() {
 *         return ResponseEntity.ok().build();
 *     }
 * }
 * </pre>
 *
 * Benefits:
 * - Declarative routing with @GetMapping
 * - Automatic content negotiation
 * - Better testability with @WebMvcTest
 * - Integration with Spring Security (if added)
 * - Automatic exception handling
 *
 * @author Claude Code Agent
 * @since Phase 2 - Spring Boot Migration - Task 5
 */
@RestController
@RequestMapping
@Profile("!test")
public class PingRestController {

    /**
     * Simple ping endpoint for health checks
     *
     * Returns HTTP 200 OK with no body.
     * Used by load balancers and monitoring tools.
     *
     * @return ResponseEntity with 200 OK status
     */
    @GetMapping("/ping")
    public ResponseEntity<Void> ping() {
        return ResponseEntity.ok().build();
    }
}
