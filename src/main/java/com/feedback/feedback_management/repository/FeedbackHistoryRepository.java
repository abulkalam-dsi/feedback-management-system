package com.feedback.feedback_management.repository;

import com.feedback.feedback_management.entity.FeedbackHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackHistoryRepository extends JpaRepository<FeedbackHistory, Long> {
    List<FeedbackHistory> findByFeedbackId(Long feedbackId);
}

