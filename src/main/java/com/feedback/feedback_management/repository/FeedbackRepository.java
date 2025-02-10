package com.feedback.feedback_management.repository;

import com.feedback.feedback_management.entity.Feedback;
import com.feedback.feedback_management.enums.FeedbackStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByStatus(FeedbackStatus status);
    // ✅ Count total feedbacks by status
    long countByStatus(FeedbackStatus status);

    // ✅ Count total feedbacks
    long count();

    // ✅ Count feedbacks grouped by category
    @Query("SELECT f.category, COUNT(f) FROM Feedback f GROUP BY f.category")
    List<Object[]> countByCategory();

    // ✅ Count feedbacks grouped by priority
    @Query("SELECT f.priority, COUNT(f) FROM Feedback f GROUP BY f.priority")
    List<Object[]> countByPriority();

    // ✅ Count feedback trends over time (last 30 days)
    @Query("SELECT f.createdAt, COUNT(f.id) FROM Feedback f WHERE f.createdAt BETWEEN :startDate AND :endDate GROUP BY f.createdAt")
    List<Object[]> countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
