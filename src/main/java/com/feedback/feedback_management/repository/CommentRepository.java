package com.feedback.feedback_management.repository;

import com.feedback.feedback_management.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByFeedbackId(Long feedbackId);
}
