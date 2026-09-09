-- Add group_id column to action_reason table for ReasonGroup enum
-- If table has 'group' column (reserved word), rename it; otherwise add new column
-- Oracle syntax - adjust for your DB if needed
ALTER TABLE action_reason ADD group_id VARCHAR2(50);
