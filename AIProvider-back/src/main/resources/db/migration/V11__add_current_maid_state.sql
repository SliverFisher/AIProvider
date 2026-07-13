ALTER TABLE MaidStates
    ADD COLUMN IsCurrent INT NOT NULL DEFAULT 0 AFTER InteractionCount;

CREATE INDEX IX_MaidStates_IsCurrent
    ON MaidStates (IsCurrent);
