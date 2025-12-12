package com.bhupendra.cloudshareapi.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/healthz")
public class HealthController {

    @Value("${HEALTH_CHECK_SECRET:}")
    private String healthSecret;

    @GetMapping
    public ResponseEntity<String> health(@RequestHeader(value = "X-Health-Token", required = false) String token) {
        if (healthSecret != null && !healthSecret.isEmpty()) {
            if (token == null || !token.equals(healthSecret)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
            }
        }
        // You can add checks for DB connectivity, Supabase reachability, etc.
        return ResponseEntity.ok("OK");
    }
}

