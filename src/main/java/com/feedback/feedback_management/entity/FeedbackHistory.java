package com.feedback.feedback_management.entity;

import com.feedback.feedback_management.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "feedback_history")
public class FeedbackHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @ManyToOne
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Enumerated(EnumType.STRING)
    private FeedbackStatus previousStatus;

    @Enumerated(EnumType.STRING)
    private FeedbackStatus newStatus;

    @Enumerated(EnumType.STRING)
    private FeedbackPriority previousPriority;

    @Enumerated(EnumType.STRING)
    private FeedbackPriority newPriority;

    @Column(nullable = false, updatable = false)
    private LocalDateTime changeTimestamp = LocalDateTime.now();
}
