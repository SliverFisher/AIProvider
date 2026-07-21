ALTER TABLE c_AsrTranscriptionRecords
  ADD COLUMN RateLimitRequestsLimit BIGINT NULL AFTER ProcessingTimeMs,
  ADD COLUMN RateLimitRequestsRemaining BIGINT NULL AFTER RateLimitRequestsLimit,
  ADD COLUMN RateLimitRequestsResetAfter VARCHAR(64) NULL AFTER RateLimitRequestsRemaining,
  ADD COLUMN RateLimitCapturedAt DATETIME(6) NULL AFTER RateLimitRequestsResetAfter,
  ADD KEY IX_AsrTranscriptionRecords_QuotaSnapshot (Provider, Model, RateLimitCapturedAt DESC);
