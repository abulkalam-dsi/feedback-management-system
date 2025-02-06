CREATE TABLE feedback_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feedback_id BIGINT NOT NULL,
    changed_by BIGINT NOT NULL,
    previous_status ENUM('PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED', 'APPROVED') NULL,
    new_status ENUM('PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED', 'APPROVED') NULL,
    previous_priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NULL,
    new_priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NULL,
    change_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (feedback_id) REFERENCES feedback(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE CASCADE
);