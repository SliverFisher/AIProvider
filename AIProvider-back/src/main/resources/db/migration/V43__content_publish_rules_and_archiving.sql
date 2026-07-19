ALTER TABLE c_ContentAccountSources
  ADD COLUMN PublishTiming VARCHAR(20) NOT NULL DEFAULT 'IMMEDIATE' AFTER Enabled,
  ADD COLUMN PublishIntervalMinutes INT NOT NULL DEFAULT 30 AFTER PublishTiming,
  ADD COLUMN LastDispatchedAt DATETIME(3) NULL AFTER PublishIntervalMinutes;

ALTER TABLE c_ContentAccounts
  ADD COLUMN ArchivedAt DATETIME(3) NULL AFTER LastPublishedAt;

ALTER TABLE c_ContentSources
  ADD COLUMN ArchivedAt DATETIME(3) NULL AFTER LastTestedAt;

CREATE TABLE IF NOT EXISTS c_ContentCollectionAccounts (
  Id BIGINT NOT NULL AUTO_INCREMENT,
  Platform VARCHAR(30) NOT NULL,
  DisplayName VARCHAR(100) NOT NULL,
  AdapterType VARCHAR(40) NOT NULL,
  CredentialEncrypted TEXT NOT NULL,
  CredentialHint VARCHAR(20) NULL,
  Enabled BOOLEAN NOT NULL DEFAULT TRUE,
  LegacySourceId BIGINT NULL,
  CreatedAt DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UpdatedAt DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (Id),
  UNIQUE KEY UX_ContentCollectionAccounts_LegacySource (LegacySourceId),
  KEY IX_ContentCollectionAccounts_PlatformEnabled (Platform, Enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE c_ContentCollectionAccounts
  ADD COLUMN ArchivedAt DATETIME(3) NULL AFTER Enabled;

CREATE INDEX IX_ContentAccountSources_Dispatch
  ON c_ContentAccountSources(Enabled, PublishTiming, LastDispatchedAt);
