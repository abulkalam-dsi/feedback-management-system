package com.feedback.feedback_management.util;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FeedbackUpdatedEvent extends ApplicationEvent {
    private final Long feedbackId;

    public FeedbackUpdatedEvent(Object source, Long feedbackId) {
        super(source);
        this.feedbackId = feedbackId;
    }
}
