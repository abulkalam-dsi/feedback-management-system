package com.feedback.feedback_management.service;

import com.feedback.feedback_management.dto.DashboardStatsDTO;
import com.feedback.feedback_management.enums.FeedbackStatus;
import com.feedback.feedback_management.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final FeedbackRepository feedbackRepository;

    public DashboardService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public DashboardStatsDTO getDashboardStats() {
        // Count feedback statuses
        long totalFeedbacks = feedbackRepository.count();
        long approvedFeedbacks = feedbackRepository.countByStatus(FeedbackStatus.APPROVED);
        long rejectedFeedbacks = feedbackRepository.countByStatus(FeedbackStatus.REJECTED);
        long pendingFeedbacks = feedbackRepository.countByStatus(FeedbackStatus.PENDING);

        // Feedback count by category
        List<Map<String, Object>> feedbackByCategory = feedbackRepository.countByCategory()
                .stream()
                .map(row -> Map.of("category", row[0], "count", row[1]))
                .collect(Collectors.toList());

        // Feedback count by priority
        List<Map<String, Object>> feedbackByPriority = feedbackRepository.countByPriority()
                .stream()
                .map(row -> Map.of("priority", row[0], "count", row[1]))
                .collect(Collectors.toList());

        // 🔥 Convert LocalDate to LocalDateTime
        LocalDateTime startDate = LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime endDate = LocalDate.now().atTime(LocalTime.MAX);

        // Fix the query to match LocalDateTime
        List<Map<String, Object>> feedbackTrends = feedbackRepository.countByDateRange(startDate, endDate)
                .stream()
                .map(row -> Map.of("date", row[0], "count", row[1]))
                .collect(Collectors.toList());

        // Return data
        return new DashboardStatsDTO(totalFeedbacks, approvedFeedbacks, rejectedFeedbacks, pendingFeedbacks,
                feedbackByCategory, feedbackByPriority, feedbackTrends);
    }
}
