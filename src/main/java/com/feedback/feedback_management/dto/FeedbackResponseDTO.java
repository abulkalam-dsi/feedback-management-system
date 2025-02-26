package com.feedback.feedback_management.dto;

import com.feedback.feedback_management.entity.Feedback;
import com.feedback.feedback_management.entity.User;
import com.feedback.feedback_management.enums.FeedbackCategory;
import com.feedback.feedback_management.enums.FeedbackPriority;
import com.feedback.feedback_management.enums.FeedbackStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

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
    private List<ApproverDTO> approvers;
    private List<ApproverDTO> approvedApprovers;
    private List<Map<String, Object>> comments;
    private LocalDateTime approvalDate;
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
        this.approvers = feedback.getApprovers().stream()
                .map(user -> new ApproverDTO(user.getId(), user.getName()))
                .collect(Collectors.toList());
        this.approvedApprovers = feedback.getApprovedApprovers().stream() // ✅ Fetch approved approvers
                .map(user -> new ApproverDTO(user.getId(), user.getName()))
                .collect(Collectors.toList());
        this.comments = feedback.getFeedbackHistory().stream()
                .filter(h -> h.getComment() != null)
                .map(h -> {
                    Map<String, Object> commentData = new HashMap<>();
                    commentData.put("comment", h.getComment());
                    commentData.put("changedBy", h.getChangedBy().getName());
                    commentData.put("changeTimestamp", h.getChangeTimestamp()); // ✅ Include timestamp
                    return commentData;
                })
                .collect(Collectors.toList());
        this.approvalDate = feedback.getApprovalDate();
        this.createdAt = feedback.getCreatedAt();
        this.updatedAt = feedback.getUpdatedAt();
    }
}

