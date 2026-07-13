package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.service.HealthService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
public class HealthController {
    private final HealthService healthService;
    public HealthController(HealthService healthService) { this.healthService = healthService; }

    @GetMapping({"/api/health", "/health"})
    public Result<Map<String, Object>> health() {
        return Result.success(healthService.check().asLegacyResponse());
    }
}
