ALTER TABLE c_ComfyParameterSchemes
  ADD COLUMN PromptMode VARCHAR(16) NOT NULL DEFAULT 'tags' AFTER Name,
  ADD CONSTRAINT CK_ComfyParameterSchemes_PromptMode CHECK (PromptMode IN ('tags', 'prose')),
  ADD KEY IX_ComfyParameterSchemes_PromptMode (PromptMode, UpdatedAt);
