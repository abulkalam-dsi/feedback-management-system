package com.feedback.feedback_management.repository;

import com.feedback.feedback_management.entity.FeedbackHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackHistoryRepository extends JpaRepository<FeedbackHistory, Long>, JpaSpecificationExecutor<FeedbackHistory> {
    @Query("SELECT fh FROM FeedbackHistory fh WHERE fh.feedback.id = :feedbackId")
    List<FeedbackHistory> findByFeedbackId(@Param("feedbackId") Long feedbackId);
}

