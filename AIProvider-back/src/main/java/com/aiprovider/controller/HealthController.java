package com.aiprovider.controller;

import com.aiprovider.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Result<Map<String, Object>> health() {
        return Result.success(Collections.singletonMap("status", "ok"));
    }
}