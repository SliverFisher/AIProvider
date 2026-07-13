package com.aiprovider.repository;

import com.aiprovider.mapper.BusinessInsightsMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class BusinessInsightsRepository {

    private final BusinessInsightsMapper insightsMapper;

    public BusinessInsightsRepository(BusinessInsightsMapper insightsMapper) {
        this.insightsMapper = insightsMapper;
    }

    public Map<String, Long> countAll(List<String> tables) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String table : tables) {
            try {
                Long value = insightsMapper.count(table);
                counts.put(table, value == null ? 0 : value);
            } catch (Exception ignored) {
                counts.put(table, 0L);
            }
        }
        return counts;
    }

    public Map<String, Object> queryFirst(String sql) {
        try {
            List<Map<String, Object>> rows = insightsMapper.queryList(sql);
            return rows.isEmpty() ? Collections.<String, Object>emptyMap() : rows.get(0);
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    public List<Map<String, Object>> queryList(String sql) {
        try {
            return insightsMapper.queryList(sql);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public Map<String, Object> runtimeState() {
        try {
            return insightsMapper.runtimeState();
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    public List<Map<String, Object>> activeReminders() {
        try {
            return insightsMapper.activeReminders();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> recentNotes() {
        try {
            return insightsMapper.recentNotes();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> recentVoiceLogs() {
        try {
            return insightsMapper.recentVoiceLogs();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> recentVideos() {
        try {
            return insightsMapper.recentVideos();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> recentRemoteVideos() {
        try {
            return insightsMapper.recentRemoteVideos();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> voiceRoles() {
        try {
            return insightsMapper.voiceRoles();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public Map<String, Object> currentMaidState() {
        try {
            Map<String, Object> state = insightsMapper.currentMaidState();
            return state == null ? Collections.<String, Object>emptyMap() : state;
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    public Map<String, Object> maidState(String roleId) {
        try {
            Map<String, Object> state = insightsMapper.maidState(roleId);
            return state == null ? Collections.<String, Object>emptyMap() : state;
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    public Map<String, Object> maidRoleSummary(String roleId) {
        try {
            Map<String, Object> summary = insightsMapper.maidRoleSummary(roleId);
            return summary == null ? Collections.<String, Object>emptyMap() : summary;
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    public List<Map<String, Object>> maidRoleDaily(String roleId) {
        try {
            return insightsMapper.maidRoleDaily(roleId);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> maidRoleRecentCalls(String roleId) {
        try {
            return insightsMapper.maidRoleRecentCalls(roleId);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}
