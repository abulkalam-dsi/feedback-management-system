INSERT IGNORE INTO feedback_approved_approvers (feedback_id, user_id)
SELECT f.id, fa.user_id
FROM feedback f
JOIN feedback_approvers fa ON f.id = fa.feedback_id
WHERE f.status = 'APPROVED';

