-- Align AIProvider's synced schema with AI_maid voice-role schema v7 and the
-- database-backed LLM source/business configuration introduced with it.

SET @has_cards = (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maid_VoiceRoleCards');
SET @has_card_json = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maid_VoiceRoleCards' AND COLUMN_NAME = 'CardJson');
SET @has_source_card = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maid_VoiceRoleCards' AND COLUMN_NAME = 'SourceCardJson');
SET @sql = IF(@has_cards = 1 AND @has_card_json = 1 AND @has_source_card = 0,
    'ALTER TABLE `maid_VoiceRoleCards` CHANGE COLUMN `CardJson` `SourceCardJson` LONGTEXT NOT NULL',
    IF(@has_cards = 1 AND @has_source_card = 0,
       'ALTER TABLE `maid_VoiceRoleCards` ADD COLUMN `SourceCardJson` LONGTEXT NULL',
       'SELECT 1'));
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @sql = IF(@has_cards = 1 AND (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maid_VoiceRoleCards' AND COLUMN_NAME = 'TemplateCardJson') = 0, 'ALTER TABLE `maid_VoiceRoleCards` ADD COLUMN `TemplateCardJson` LONGTEXT NULL', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;
SET @sql = IF(@has_cards = 1 AND (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maid_VoiceRoleCards' AND COLUMN_NAME = 'TemplateCardGeneratedAt') = 0, 'ALTER TABLE `maid_VoiceRoleCards` ADD COLUMN `TemplateCardGeneratedAt` DATETIME(6) NULL', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;
SET @sql = IF(@has_cards = 1 AND (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maid_VoiceRoleCards' AND COLUMN_NAME = 'TemplateCardIterationCount') = 0, 'ALTER TABLE `maid_VoiceRoleCards` ADD COLUMN `TemplateCardIterationCount` INT NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;
SET @sql = IF(@has_cards = 1 AND (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maid_VoiceRoleCards' AND COLUMN_NAME = 'TemplateCardSourceHash') = 0, 'ALTER TABLE `maid_VoiceRoleCards` ADD COLUMN `TemplateCardSourceHash` VARCHAR(64) NOT NULL DEFAULT ''''', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;
SET @sql = IF(@has_cards = 1 AND (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maid_VoiceRoleCards' AND COLUMN_NAME = 'TemplateCardGenerationStatus') = 0, 'ALTER TABLE `maid_VoiceRoleCards` ADD COLUMN `TemplateCardGenerationStatus` VARCHAR(32) NOT NULL DEFAULT ''missing''', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;
SET @sql = IF(@has_cards = 1 AND (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maid_VoiceRoleCards' AND COLUMN_NAME = 'TemplateCardGenerationMessage') = 0, 'ALTER TABLE `maid_VoiceRoleCards` ADD COLUMN `TemplateCardGenerationMessage` LONGTEXT NULL', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;
SET @sql = IF(@has_cards = 1 AND (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maid_VoiceRoleCards' AND COLUMN_NAME = 'TemplateCardLastAttemptAt') = 0, 'ALTER TABLE `maid_VoiceRoleCards` ADD COLUMN `TemplateCardLastAttemptAt` DATETIME(6) NULL', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

CREATE TABLE IF NOT EXISTS `maid_LlmSourcePrompts` (
    `Id` BIGINT NOT NULL,
    `SourceKey` VARCHAR(96) NOT NULL,
    `Purpose` VARCHAR(256) NOT NULL,
    `SystemPromptTemplate` LONGTEXT NOT NULL,
    `UserPromptTemplate` LONGTEXT NOT NULL,
    `OutputSchemaJson` LONGTEXT NOT NULL,
    `IsEnabled` BIT(1) NOT NULL DEFAULT b'1',
    `CreatedAt` DATETIME(6) NOT NULL,
    `UpdatedAt` DATETIME(6) NOT NULL,
    `UserId` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`Id`),
    UNIQUE KEY `UK_maid_LlmSourcePrompts_SourceKey` (`SourceKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `maid_LlmBusinessModelConfigs` (
    `Id` BIGINT NOT NULL,
    `BusinessKey` VARCHAR(96) NOT NULL,
    `DisplayName` VARCHAR(128) NOT NULL,
    `Description` VARCHAR(512) NOT NULL,
    `Provider` VARCHAR(32) NOT NULL,
    `ModelKey` VARCHAR(128) NOT NULL,
    `IsEnabled` BIT(1) NOT NULL DEFAULT b'1',
    `CreatedAt` DATETIME(6) NOT NULL,
    `UpdatedAt` DATETIME(6) NOT NULL,
    `UserId` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`Id`),
    UNIQUE KEY `UK_maid_LlmBusinessModelConfigs_BusinessKey` (`BusinessKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
