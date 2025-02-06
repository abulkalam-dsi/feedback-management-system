package com.feedback.feedback_management.specification;

import com.feedback.feedback_management.entity.FeedbackHistory;
import com.feedback.feedback_management.enums.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedbackHistorySpecification implements Specification<FeedbackHistory> {
    private final Long feedbackId;
    private final String changedBy;
    private final FeedbackStatus previousStatus;
    private final FeedbackStatus newStatus;
    private final FeedbackPriority previousPriority;
    private final FeedbackPriority newPriority;
    private final LocalDateTime fromDate;
    private final LocalDateTime toDate;

    public FeedbackHistorySpecification(Long feedbackId, String changedBy, FeedbackStatus previousStatus,
                                        FeedbackStatus newStatus, FeedbackPriority previousPriority,
                                        FeedbackPriority newPriority, LocalDateTime fromDate, LocalDateTime toDate) {
        this.feedbackId = feedbackId;
        this.changedBy = changedBy;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.previousPriority = previousPriority;
        this.newPriority = newPriority;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    @Override
    public Predicate toPredicate(Root<FeedbackHistory> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (feedbackId != null) {
            predicates.add(cb.equal(root.get("feedback").get("id"), feedbackId));
        }
        if (changedBy != null && !changedBy.isEmpty()) {
            predicates.add(cb.like(root.get("changedBy").get("name"), "%" + changedBy + "%"));
        }
        if (previousStatus != null) {
            predicates.add(cb.equal(root.get("previousStatus"), previousStatus));
        }
        if (newStatus != null) {
            predicates.add(cb.equal(root.get("newStatus"), newStatus));
        }
        if (previousPriority != null) {
            predicates.add(cb.equal(root.get("previousPriority"), previousPriority));
        }
        if (newPriority != null) {
            predicates.add(cb.equal(root.get("newPriority"), newPriority));
        }
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("changeTimestamp"), fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("changeTimestamp"), toDate));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
