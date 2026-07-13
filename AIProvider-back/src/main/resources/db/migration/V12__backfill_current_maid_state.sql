UPDATE MaidStates state
JOIN (
    SELECT LastRole
    FROM AppRuntimeStates
    ORDER BY UpdatedAt DESC
    LIMIT 1
) runtime ON 1 = 1
SET state.IsCurrent = CASE
    WHEN LOWER(state.MaidId) = LOWER(runtime.LastRole) THEN 1
    ELSE 0
END;
