package com.aiprovider.model.vo;

import java.util.Map;

public class SyncResultVO {

    private int saved;
    private Map<String, Integer> tables;
    private String syncedAt;

    public int getSaved() { return saved; }
    public void setSaved(int saved) { this.saved = saved; }
    public Map<String, Integer> getTables() { return tables; }
    public void setTables(Map<String, Integer> tables) { this.tables = tables; }
    public String getSyncedAt() { return syncedAt; }
    public void setSyncedAt(String syncedAt) { this.syncedAt = syncedAt; }
}