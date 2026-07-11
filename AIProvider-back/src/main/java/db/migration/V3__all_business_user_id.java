package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import java.sql.*;

public class V3__all_business_user_id extends BaseJavaMigration {
    private static final String[] TABLES = {
        "TimerRecords","AppSettings","AiConversations","MaidStates","VoiceTriggerLogs","VoiceRoles",
        "VoiceRoleVoices","VoiceAssets","VoiceRoleAudioCaches","VoiceRoleBindings","VoiceRoleCards",
        "ProactiveTriggerRules","ProactiveTriggerStates","DisturbanceSettings","UserProfiles","AppRuntimeStates",
        "Reminders","ReminderLogs","ChatCommandLaunchers","NotebookNotes","NotebookAttachments","ActionTagDefinitions",
        "DesktopContextSnapshots","ProactiveBroadcastSourceSettings","ProactiveBroadcastTriggerLogs","LlmCallLogs",
        "DbColumnComments","LlmChatConversations","LlmChatMessages","VoiceCacheDedupeLogs","LlmProviderSelections",
        "ChatMessages","VoiceConversations","AgentCapabilities","AgentToolCalls","VaultItems","VaultItemHistories",
        "VideoItems","VideoAlbums","VideoTagDefinitions","VideoSiteConfigs","VideoPlaybackHistories","VideoSubtitleBindings",
        "RemoteVideoItems","RemoteDownloadTasks","RemotePlayHistories","RemoteAuthors","RemoteVideoSettings"
    };
    @Override public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        for (String table : TABLES) {
            if (!tableExists(connection, table) || columnExists(connection, table, "UserId")) continue;
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE `" + table + "` ADD COLUMN `UserId` BIGINT NOT NULL DEFAULT 0");
            }
        }
    }
    private static boolean tableExists(Connection c, String table) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?")) {
            s.setString(1, table); try (ResultSet r = s.executeQuery()) { r.next(); return r.getInt(1) > 0; }
        }
    }
    private static boolean columnExists(Connection c, String table, String column) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?")) {
            s.setString(1, table); s.setString(2, column); try (ResultSet r = s.executeQuery()) { r.next(); return r.getInt(1) > 0; }
        }
    }
}
