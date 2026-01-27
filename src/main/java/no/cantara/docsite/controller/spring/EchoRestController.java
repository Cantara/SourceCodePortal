package no.cantara.docsite.controller.spring;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Echo REST Controller for Spring Boot
 * <p>
 * Diagnostic endpoint that echoes back request information for debugging.
 * Replaces the Undertow EchoController with Spring MVC.
 * <p>
 * Endpoints:
 * - GET  /echo - Returns 200 OK with request details
 * - POST /echo - Returns 204 NO_CONTENT with request details
 */
@RestController
@RequestMapping("/echo")
public class EchoRestController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> echoGet(HttpServletRequest request,
                                                         @RequestHeader Map<String, String> headers,
                                                         @RequestParam Map<String, String> queryParams,
                                                         @RequestBody(required = false) String body) {
        Map<String, Object> response = buildEchoResponse(request, headers, queryParams, body);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> echoPost(HttpServletRequest request,
                                                          @RequestHeader Map<String, String> headers,
                                                          @RequestParam Map<String, String> queryParams,
                                                          @RequestBody(required = false) String body) {
        Map<String, Object> response = buildEchoResponse(request, headers, queryParams, body);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    private Map<String, Object> buildEchoResponse(HttpServletRequest request,
                                                    Map<String, String> headers,
                                                    Map<String, String> queryParams,
                                                    String body) {
        Map<String, Object> response = new LinkedHashMap<>();

        // Request headers
        response.put("request-headers", headers);

        // Request info
        Map<String, Object> requestInfo = new LinkedHashMap<>();
        requestInfo.put("uri", request.getRequestURI());
        requestInfo.put("method", request.getMethod());
        requestInfo.put("statusCode", "200");
        requestInfo.put("isSecure", String.valueOf(request.isSecure()));
        requestInfo.put("sourceAddress", request.getRemoteAddr());
        requestInfo.put("destinationAddress", request.getLocalAddr());
        response.put("request-info", requestInfo);

        // Cookies
        Map<String, String> cookies = new LinkedHashMap<>();
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                cookies.put(cookie.getName(), cookie.getValue());
            }
        }
        response.put("cookies", cookies);

        // Path parameters (not applicable for /echo, but included for consistency)
        response.put("path-parameters", Collections.emptyMap());

        // Query parameters
        response.put("queryString", request.getQueryString());
        response.put("query-parameters", queryParams);

        // Request body
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("payload", body != null ? body : "");
        response.put("contentLength", String.valueOf(request.getContentLength()));
        response.put("request-body", requestBody);

        // Response headers (will be added by Spring automatically)
        response.put("response-headers", Collections.emptyMap());

        // Response cookies (none for this endpoint)
        response.put("response-cookies", Collections.emptyMap());

        return response;
    }
}
