package com.codibly.energymix.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Lightweight liveness endpoint used by the hosting platform's health check. */
@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "energy-mix-backend");
    }
}
