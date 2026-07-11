package com.aiprovider.repository;

import com.aiprovider.mapper.DashboardMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DashboardRepository {

    private final DashboardMapper dashboardMapper;

    public DashboardRepository(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    public long count(String tableName) {
        try {
            return Optional.ofNullable(dashboardMapper.count(tableName)).orElse(0L);
        } catch (Exception e) {
            return 0L;
        }
    }

    public Map<String, Object> llmAggregation() {
        try {
            return dashboardMapper.llmAggregation();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public Map<String, Object> timeAggregation() {
        try {
            return dashboardMapper.timeAggregation();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public Map<String, Object> agentStats() {
        try {
            return dashboardMapper.agentStats();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public Map<String, Object> desktopStats() {
        try {
            return dashboardMapper.desktopStats();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public Map<String, Object> latestMaidState() {
        try {
            return dashboardMapper.latestMaidState();
        } catch (Exception e) {
            return null;
        }
    }

    public List<Map<String, Object>> llmUsageDaily(int days) {
        try {
            return dashboardMapper.llmUsageDaily(days);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> llmModelStats() {
        try {
            return dashboardMapper.llmModelStats();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> timeTrackingDaily(int days) {
        try {
            return dashboardMapper.timeTrackingDaily(days);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> agentToolUsage() {
        try {
            return dashboardMapper.agentToolUsage();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> desktopAppUsage() {
        try {
            return dashboardMapper.desktopAppUsage();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> broadcastStats() {
        try {
            return dashboardMapper.broadcastStats();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> recentChats(int limit) {
        try {
            return dashboardMapper.recentChats(limit);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> recentLlmCalls(int limit) {
        try {
            return dashboardMapper.recentLlmCalls(limit);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Map<String, Object> chatStats() {
        try {
            return dashboardMapper.chatStats();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}