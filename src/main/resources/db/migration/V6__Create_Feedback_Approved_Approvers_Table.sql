CREATE TABLE IF NOT EXISTS feedback_approved_approvers (
    feedback_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (feedback_id, user_id),
    FOREIGN KEY (feedback_id) REFERENCES feedback(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
