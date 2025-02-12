CREATE TABLE feedback_approvers (
    feedback_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (feedback_id, user_id),
    CONSTRAINT fk_feedback FOREIGN KEY (feedback_id) REFERENCES feedback (id) ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ✅ 2. Remove the old approver_id column (if exists)
ALTER TABLE feedback DROP COLUMN IF EXISTS approver_id;

-- ✅ 3. Add approval_date column if not exists
ALTER TABLE feedback ADD COLUMN IF NOT EXISTS approval_date TIMESTAMP NULL;
