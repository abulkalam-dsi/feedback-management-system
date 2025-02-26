INSERT INTO feedback_approved_approvers (feedback_id, user_id)
SELECT f.id, u.id
FROM feedback f
JOIN feedback_approvers fa ON f.id = fa.feedback_id
JOIN users u ON fa.user_id = u.id
WHERE f.status = 'APPROVED';
