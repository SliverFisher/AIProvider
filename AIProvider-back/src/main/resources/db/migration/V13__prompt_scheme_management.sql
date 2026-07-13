ALTER TABLE ComfyParameterSchemes
  DROP FOREIGN KEY FK_ComfyParameterSchemes_Workflow,
  ADD COLUMN Notes VARCHAR(1000) NULL AFTER OutputFolder,
  ADD COLUMN IsDefault BOOLEAN NOT NULL DEFAULT FALSE AFTER Notes,
  ADD KEY IX_ComfyParameterSchemes_Default (IsDefault, UpdatedAt);
