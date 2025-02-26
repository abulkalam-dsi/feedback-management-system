package com.feedback.feedback_management.dto;

import com.feedback.feedback_management.entity.Comment;
import com.feedback.feedback_management.entity.Feedback;
import com.feedback.feedback_management.entity.User;
import com.feedback.feedback_management.enums.FeedbackCategory;
import com.feedback.feedback_management.enums.FeedbackPriority;
import com.feedback.feedback_management.enums.FeedbackStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private List<CommentDTO> comments = new ArrayList<>();
    private List<Map<String, Object>> feedbackHistory;
    private LocalDateTime approvalDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FeedbackResponseDTO(Feedback feedback) {
        this(feedback, new ArrayList<>()); // Calls the main constructor with an empty list
    }

    public FeedbackResponseDTO(Feedback feedback, List<Comment> commentList) {
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
        this.comments = commentList != null ? commentList.stream()
                .map(comment -> new CommentDTO(comment.getUser().getName(), comment.getText(), comment.getCreatedAt()))
                .collect(Collectors.toList()) : new ArrayList<>();

        // ✅ Keep only status/priority changes in history
        this.feedbackHistory = feedback.getFeedbackHistory().stream()
                .map(h -> {
                    Map<String, Object> historyData = new HashMap<>();
                    historyData.put("changedBy", h.getChangedBy().getName());
                    historyData.put("previousStatus", h.getPreviousStatus());
                    historyData.put("newStatus", h.getNewStatus());
                    historyData.put("previousPriority", h.getPreviousPriority());
                    historyData.put("newPriority", h.getNewPriority());
                    historyData.put("changeTimestamp", h.getChangeTimestamp());
                    return historyData;
                })
                .collect(Collectors.toList());
        this.approvalDate = feedback.getApprovalDate();
        this.createdAt = feedback.getCreatedAt();
        this.updatedAt = feedback.getUpdatedAt();
    }
}

