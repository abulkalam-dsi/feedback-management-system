package com.feedback.feedback_management.repository;

import com.feedback.feedback_management.entity.Feedback;
import com.feedback.feedback_management.enums.FeedbackStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByStatus(FeedbackStatus status);
}
