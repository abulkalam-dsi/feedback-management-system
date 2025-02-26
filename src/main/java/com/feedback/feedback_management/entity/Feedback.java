package com.feedback.feedback_management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.feedback.feedback_management.enums.FeedbackCategory;
import com.feedback.feedback_management.enums.FeedbackPriority;
import com.feedback.feedback_management.enums.FeedbackStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackStatus status = FeedbackStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToMany
    @JoinTable(
            name = "feedback_approvers",
            joinColumns = @JoinColumn(name = "feedback_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> approvers = new HashSet<>(); // Multiple approvers

    @ManyToMany
    @JoinTable(
            name = "feedback_approved_approvers", // ✅ New table to track approvals
            joinColumns = @JoinColumn(name = "feedback_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> approvedApprovers = new HashSet<>(); // ✅ Approvers who approved

    public Set<User> getApprovedApprovers() {
        return approvedApprovers;
    }

    public void approveBy(User user) {
        this.approvedApprovers.add(user);
    }

    // ✅ Check if all assigned approvers have approved
    public boolean isFullyApproved() {
        return approvedApprovers.size() >= approvers.size();
    }

    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("changeTimestamp DESC")
    @JsonIgnore
    private List<FeedbackHistory> feedbackHistory = new ArrayList<>(); // Link to history

    //Update timestamp when modifying status
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    public List<FeedbackHistory> getFeedbackHistory() {
        return feedbackHistory;
    }
}
