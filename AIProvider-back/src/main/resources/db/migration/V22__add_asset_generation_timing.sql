ALTER TABLE c_GeneratedAssets
  ADD COLUMN GenerationCompletedAt DATETIME(3) NULL AFTER GeneratedAt,
  ADD COLUMN GenerationDurationMs BIGINT NULL AFTER GenerationCompletedAt;
