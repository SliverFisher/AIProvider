package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.service.BusinessInsightsService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/insights")
public class BusinessInsightsController {

    private final BusinessInsightsService insightsService;

    public BusinessInsightsController(BusinessInsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping("/command")
    public Result<Map<String, Object>> command() {
        return Result.success(insightsService.getCommand());
    }

    @GetMapping("/maid-role")
    public Result<Map<String, Object>> maidRole(@RequestParam String roleId) {
        return Result.success(insightsService.getMaidRole(roleId));
    }
}
