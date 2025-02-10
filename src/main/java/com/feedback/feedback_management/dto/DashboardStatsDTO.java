package com.feedback.feedback_management.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DashboardStatsDTO {
    private long totalFeedbacks;
    private long approvedFeedbacks;
    private long rejectedFeedbacks;
    private long pendingFeedbacks;
    private List<Map<String, Object>> feedbackByCategory;
    private List<Map<String, Object>> feedbackByPriority;
    private List<Map<String, Object>> feedbackTrends;

    public DashboardStatsDTO(long totalFeedbacks, long approvedFeedbacks, long rejectedFeedbacks, long pendingFeedbacks,
                             List<Map<String, Object>> feedbackByCategory, List<Map<String, Object>> feedbackByPriority,
                             List<Map<String, Object>> feedbackTrends) {
        this.totalFeedbacks = totalFeedbacks;
        this.approvedFeedbacks = approvedFeedbacks;
        this.rejectedFeedbacks = rejectedFeedbacks;
        this.pendingFeedbacks = pendingFeedbacks;
        this.feedbackByCategory = feedbackByCategory;
        this.feedbackByPriority = feedbackByPriority;
        this.feedbackTrends = feedbackTrends;
    }
}
