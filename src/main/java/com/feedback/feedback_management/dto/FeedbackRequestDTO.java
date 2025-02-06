package com.feedback.feedback_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.feedback.feedback_management.enums.FeedbackCategory;
import com.feedback.feedback_management.enums.FeedbackPriority;
import com.feedback.feedback_management.enums.FeedbackStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackRequestDTO {
    private String title;
    private String description;
    private FeedbackCategory category;
    private FeedbackPriority priority;
    private FeedbackStatus status;
    @JsonProperty("created_by")
    private Long createdBy;
    @JsonProperty("assigned_to")
    private Long assignedTo;
}
