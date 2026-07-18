ALTER TABLE c_ContentOperationSettings
  ADD COLUMN RelevancePrompt LONGTEXT NULL AFTER GeminiApiKeyHint;

UPDATE c_ContentOperationSettings SET RelevancePrompt = '你是内容选题审核员。判断来源内容是否与人工智能、机器学习、大模型、AI产品、AI公司、算力芯片、机器人或直接影响AI行业的重要事件相关。不要因为作者属于科技行业就判定相关，必须依据这条内容本身。请给出是否相关、0到1的相关度和简短原因。' WHERE Id=1;

ALTER TABLE c_ContentOperationSettings
  MODIFY COLUMN RelevancePrompt LONGTEXT NOT NULL;

ALTER TABLE c_ContentItems
  ADD COLUMN RelevanceStatus VARCHAR(30) NOT NULL DEFAULT 'PENDING' AFTER FetchedByRunType,
  ADD COLUMN RelevanceScore DECIMAL(5,4) NULL AFTER RelevanceStatus,
  ADD COLUMN RelevanceReason VARCHAR(1000) NULL AFTER RelevanceScore,
  ADD COLUMN RelevanceCheckedAt DATETIME(3) NULL AFTER RelevanceReason;

ALTER TABLE c_ContentAiGenerations
  ADD COLUMN ContentItemId BIGINT NULL AFTER Id,
  ADD KEY IX_ContentAiGenerations_ContentItem (ContentItemId, GenerationType, CreatedAt),
  ADD CONSTRAINT FK_ContentAiGenerations_ContentItem FOREIGN KEY (ContentItemId) REFERENCES c_ContentItems(Id) ON DELETE SET NULL;
