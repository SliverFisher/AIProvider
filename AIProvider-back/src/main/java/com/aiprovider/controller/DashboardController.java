package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(dashboardService.getOverview());
    }

    @GetMapping("/llm-usage-daily")
    public Result<List<Map<String, Object>>> llmUsageDaily(@RequestParam(defaultValue = "30") int days) {
        return Result.success(dashboardService.getLlmUsageDaily(days));
    }

    @GetMapping("/llm-model-stats")
    public Result<List<Map<String, Object>>> llmModelStats() {
        return Result.success(dashboardService.getLlmModelStats());
    }

    @GetMapping("/time-tracking-daily")
    public Result<List<Map<String, Object>>> timeTrackingDaily(@RequestParam(defaultValue = "30") int days) {
        return Result.success(dashboardService.getTimeTrackingDaily(days));
    }

    @GetMapping("/agent-tool-usage")
    public Result<List<Map<String, Object>>> agentToolUsage() {
        return Result.success(dashboardService.getAgentToolUsage());
    }

    @GetMapping("/desktop-app-usage")
    public Result<List<Map<String, Object>>> desktopAppUsage() {
        return Result.success(dashboardService.getDesktopAppUsage());
    }

    @GetMapping("/broadcast-stats")
    public Result<List<Map<String, Object>>> broadcastStats() {
        return Result.success(dashboardService.getBroadcastStats());
    }

    @GetMapping("/recent-chats")
    public Result<List<Map<String, Object>>> recentChats(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(dashboardService.getRecentChats(limit));
    }

    @GetMapping("/recent-llm-calls")
    public Result<List<Map<String, Object>>> recentLlmCalls(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(dashboardService.getRecentLlmCalls(limit));
    }

    @GetMapping("/chat-stats")
    public Result<Map<String, Object>> chatStats() {
        return Result.success(dashboardService.getChatStats());
    }
}