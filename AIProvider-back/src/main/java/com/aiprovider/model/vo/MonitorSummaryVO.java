package com.aiprovider.model.vo;

import java.time.OffsetDateTime;

public class MonitorSummaryVO {
    private final Health health;
    private final OffsetDateTime collectedAt;
    private final Resource memory;
    private final Resource disk;
    private final Traffic traffic;

    public MonitorSummaryVO(Health health, OffsetDateTime collectedAt, Resource memory, Resource disk, Traffic traffic) {
        this.health = health; this.collectedAt = collectedAt; this.memory = memory; this.disk = disk; this.traffic = traffic;
    }
    public Health getHealth() { return health; }
    public OffsetDateTime getCollectedAt() { return collectedAt; }
    public Resource getMemory() { return memory; }
    public Resource getDisk() { return disk; }
    public Traffic getTraffic() { return traffic; }

    public static class Health {
        private final String status; private final OffsetDateTime checkedAt;
        public Health(String status, OffsetDateTime checkedAt) { this.status = status; this.checkedAt = checkedAt; }
        public String getStatus() { return status; } public OffsetDateTime getCheckedAt() { return checkedAt; }
    }
    public static class Resource {
        private final Long usedBytes; private final Long totalBytes; private final boolean available; private final String unavailableReason;
        public Resource(Long usedBytes, Long totalBytes, boolean available, String unavailableReason) {
            this.usedBytes = usedBytes; this.totalBytes = totalBytes; this.available = available; this.unavailableReason = unavailableReason;
        }
        public Long getUsedBytes() { return usedBytes; } public Long getTotalBytes() { return totalBytes; }
        public boolean isAvailable() { return available; } public String getUnavailableReason() { return unavailableReason; }
    }
    public static class Traffic {
        private final Long usedBytes; private final Long totalBytes; private final Long remainingBytes; private final Long overflowBytes;
        private final OffsetDateTime periodStart; private final OffsetDateTime periodEnd; private final OffsetDateTime deadline;
        private final String status; private final boolean available; private final boolean stale; private final OffsetDateTime collectedAt;
        public Traffic(Long usedBytes, Long totalBytes, Long remainingBytes, Long overflowBytes,
                       OffsetDateTime periodStart, OffsetDateTime periodEnd, OffsetDateTime deadline, String status,
                       boolean available, boolean stale, OffsetDateTime collectedAt) {
            this.usedBytes=usedBytes; this.totalBytes=totalBytes; this.remainingBytes=remainingBytes; this.overflowBytes=overflowBytes;
            this.periodStart=periodStart; this.periodEnd=periodEnd; this.deadline=deadline; this.status=status;
            this.available=available; this.stale=stale; this.collectedAt=collectedAt;
        }
        public static Traffic unavailable(String status) { return new Traffic(null,null,null,null,null,null,null,status,false,false,null); }
        public Long getUsedBytes(){return usedBytes;} public Long getTotalBytes(){return totalBytes;} public Long getRemainingBytes(){return remainingBytes;}
        public Long getOverflowBytes(){return overflowBytes;} public OffsetDateTime getPeriodStart(){return periodStart;} public OffsetDateTime getPeriodEnd(){return periodEnd;}
        public OffsetDateTime getDeadline(){return deadline;} public String getStatus(){return status;} public boolean isAvailable(){return available;}
        public boolean isStale(){return stale;} public OffsetDateTime getCollectedAt(){return collectedAt;}
        public Traffic asStale() { return new Traffic(usedBytes,totalBytes,remainingBytes,overflowBytes,periodStart,periodEnd,deadline,status,available,true,collectedAt); }
    }
}
