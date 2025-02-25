-- Check if the column exists before dropping
SET @exist = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
              WHERE TABLE_NAME = 'feedback' 
              AND COLUMN_NAME = 'approver_id' 
              AND TABLE_SCHEMA = DATABASE());

SET @query = IF(@exist > 0, 'ALTER TABLE feedback DROP COLUMN approver_id', 'SELECT 1');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Create the many-to-many relationship table for approvers
CREATE TABLE IF NOT EXISTS feedback_approvers (
    feedback_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (feedback_id, user_id),
    CONSTRAINT fk_feedback FOREIGN KEY (feedback_id) REFERENCES feedback (id) ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Add `approval_date` column only if it doesn't exist
SET @exist = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
              WHERE TABLE_NAME = 'feedback' 
              AND COLUMN_NAME = 'approval_date' 
              AND TABLE_SCHEMA = DATABASE());

SET @query = IF(@exist = 0, 'ALTER TABLE feedback ADD COLUMN approval_date TIMESTAMP NULL', 'SELECT 1');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
