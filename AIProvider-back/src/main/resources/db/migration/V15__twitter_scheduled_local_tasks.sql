ALTER TABLE TwitterPosts
    ADD COLUMN ScheduledAt DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) AFTER AttemptCount,
    ADD COLUMN Source VARCHAR(20) NOT NULL DEFAULT 'MANUAL' AFTER ScheduledAt,
    ADD KEY IX_TwitterPosts_Account_Status_Scheduled (AccountId, Status, ScheduledAt);

ALTER TABLE TwitterPostMedia
    MODIFY COLUMN StoragePath VARCHAR(1000) NULL,
    MODIFY COLUMN Sha256 CHAR(64) NULL,
    ADD COLUMN LocalPath VARCHAR(2000) NULL AFTER StoragePath,
    ADD COLUMN LocalSource VARCHAR(20) NULL AFTER LocalPath;
