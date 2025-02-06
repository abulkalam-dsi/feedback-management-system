package com.feedback.feedback_management.dto;


import com.feedback.feedback_management.entity.FeedbackHistory;
import com.feedback.feedback_management.enums.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackHistoryResponseDTO {
    private Long id;
    private Long feedbackId;
    private String changedBy;
    private FeedbackStatus previousStatus;
    private FeedbackStatus newStatus;
    private FeedbackPriority previousPriority;
    private FeedbackPriority newPriority;
    private LocalDateTime changeTimestamp;

    // Constructor to map Entity to DTO
    public FeedbackHistoryResponseDTO(FeedbackHistory history) {
        this.id = history.getId();
        this.feedbackId = history.getFeedback().getId();
        this.changedBy = history.getChangedBy().getName(); // Get User's name
        this.previousStatus = history.getPreviousStatus();
        this.newStatus = history.getNewStatus();
        this.previousPriority = history.getPreviousPriority();
        this.newPriority = history.getNewPriority();
        this.changeTimestamp = history.getChangeTimestamp();
    }
}

