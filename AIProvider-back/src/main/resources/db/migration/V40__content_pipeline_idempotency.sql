ALTER TABLE c_ContentDrafts
  ADD UNIQUE KEY UX_ContentDrafts_ItemPlatform (ContentItemId, Platform);

ALTER TABLE c_ContentPublications
  ADD UNIQUE KEY UX_ContentPublications_DraftAccount (DraftId, AccountId);

ALTER TABLE c_ContentAccounts
  ADD COLUMN AdapterType VARCHAR(40) NOT NULL DEFAULT 'XHS_WEB' AFTER PublishMode,
  ADD COLUMN SessionEncrypted LONGTEXT NULL AFTER AdapterStatus,
  ADD COLUMN SessionHint VARCHAR(80) NULL AFTER SessionEncrypted,
  ADD COLUMN LastConnectedAt DATETIME(3) NULL AFTER LastError;
