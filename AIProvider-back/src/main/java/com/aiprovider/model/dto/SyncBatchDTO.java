package com.aiprovider.model.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;

public class SyncBatchDTO {

    private String deviceId;
    private List<BusinessRecord> records;

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public List<BusinessRecord> getRecords() { return records; }
    public void setRecords(List<BusinessRecord> records) { this.records = records; }

    public static class BusinessRecord {
        private String table;
        private String operation;
        private Instant updatedAt;
        private JsonNode payload;

        public String getTable() { return table; }
        public void setTable(String table) { this.table = table; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public Instant getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
        public JsonNode getPayload() { return payload; }
        public void setPayload(JsonNode payload) { this.payload = payload; }
    }
}