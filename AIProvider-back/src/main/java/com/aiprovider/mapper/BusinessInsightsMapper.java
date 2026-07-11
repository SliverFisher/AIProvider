package com.aiprovider.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface BusinessInsightsMapper {

    @Select("SELECT COUNT(*) FROM `${tableName}`")
    Long count(@Param("tableName") String tableName);

    @Select("${sql}")
    List<Map<String, Object>> queryList(@Param("sql") String sql);

    @Select("SELECT LastRole, LastModel, LastVoiceId, LastInteractionAt, DisturbanceMode, " +
            "TtsStatus, OllamaStatus, LastLlmLatencyMs, LastTtsLatencyMs, UpdatedAt " +
            "FROM AppRuntimeStates ORDER BY UpdatedAt DESC LIMIT 1")
    Map<String, Object> runtimeState();

    @Select("SELECT Id, Title, Message, DueAt, NextDueAt, Enabled, AllowTts, LastTriggeredAt " +
            "FROM Reminders WHERE Enabled = 1 ORDER BY COALESCE(NextDueAt, DueAt) ASC LIMIT 6")
    List<Map<String, Object>> activeReminders();

    @Select("SELECT Id, Title, LEFT(ContentPlainText, 160) AS Preview, IsPinned, CreatedAt, UpdatedAt " +
            "FROM NotebookNotes WHERE IsDeleted = 0 ORDER BY IsPinned DESC, UpdatedAt DESC LIMIT 6")
    List<Map<String, Object>> recentNotes();

    @Select("SELECT Id, Source, RoleId, Category, Played, Reason, LEFT(Text, 100) AS Text, CreatedAt " +
            "FROM VoiceTriggerLogs ORDER BY CreatedAt DESC LIMIT 8")
    List<Map<String, Object>> recentVoiceLogs();

    @Select("SELECT Id, Title, SourceType, DurationSeconds, LastPositionSeconds, IsFavorite, IsCompleted, " +
            "LastPlayedAt, CreatedAt FROM VideoItems ORDER BY COALESCE(LastPlayedAt, CreatedAt) DESC LIMIT 6")
    List<Map<String, Object>> recentVideos();

    @Select("SELECT Id, SiteName, Title, AuthorName, DurationText, DownloadStatus, LastResolvedAt, CreatedAt " +
            "FROM RemoteVideoItems ORDER BY COALESCE(LastResolvedAt, CreatedAt) DESC LIMIT 6")
    List<Map<String, Object>> recentRemoteVideos();

    @Select("SELECT RoleId, DisplayName, IsEnabled, UpdatedAt " +
            "FROM VoiceRoles WHERE IsEnabled = 1 ORDER BY SortOrder, DisplayName LIMIT 12")
    List<Map<String, Object>> voiceRoles();
}