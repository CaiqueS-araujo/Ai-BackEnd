package com.serratec.chat.controller;

import com.serratec.chat.dto.response.HealthResponse;
import com.serratec.chat.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public ResponseEntity<HealthResponse> check() {
        HealthResponse health = healthService.check();
        if ("DOWN".equals(health.status())) {
            return ResponseEntity.status(503).body(health);
        }
        return ResponseEntity.ok(health);
    }
}
