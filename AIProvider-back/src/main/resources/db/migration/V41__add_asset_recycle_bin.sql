ALTER TABLE c_GeneratedAssets
  ADD COLUMN TrashOriginalStatus VARCHAR(16) NULL AFTER Status,
  ADD INDEX IX_GeneratedAssets_Platform_TrashOriginalStatus (Platform, TrashOriginalStatus);
