ALTER TABLE TwitterPostMedia
    ADD COLUMN AssetId BIGINT NULL AFTER PostId,
    ADD KEY IX_TwitterPostMedia_Asset (AssetId),
    ADD CONSTRAINT FK_TwitterPostMedia_Asset FOREIGN KEY (AssetId) REFERENCES GeneratedAssets(Id) ON DELETE SET NULL;
