package com.feedback.feedback_management.dto;

import com.feedback.feedback_management.entity.Feedback;
import com.feedback.feedback_management.enums.FeedbackCategory;
import com.feedback.feedback_management.enums.FeedbackPriority;
import com.feedback.feedback_management.enums.FeedbackStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FeedbackResponseDTO {
    private Long id;
    private String title;
    private String description;
    private FeedbackCategory category;
    private FeedbackPriority priority;
    private FeedbackStatus status;
    private String createdBy;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FeedbackResponseDTO(Feedback feedback) {
        this.id = feedback.getId();
        this.title = feedback.getTitle();
        this.description = feedback.getDescription();
        this.category = feedback.getCategory();
        this.priority = feedback.getPriority();
        this.status = feedback.getStatus();
        this.createdBy = feedback.getCreatedBy().getName();
        this.assignedTo = (feedback.getAssignedTo() != null) ? feedback.getAssignedTo().getName() : "Unassigned";
        this.createdAt = feedback.getCreatedAt();
        this.updatedAt = feedback.getUpdatedAt();
    }
}

