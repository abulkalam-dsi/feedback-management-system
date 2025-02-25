package com.feedback.feedback_management.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.feedback_management.dto.FeedbackResponseDTO;
import com.feedback.feedback_management.entity.Feedback;
import com.feedback.feedback_management.repository.FeedbackRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class FeedbackEventListener {
    private final FeedbackRepository feedbackRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    @PersistenceContext
    private EntityManager entityManager;

    public FeedbackEventListener(FeedbackRepository feedbackRepository,
                                 SimpMessagingTemplate messagingTemplate,
                                 ObjectMapper objectMapper) {
        this.feedbackRepository = feedbackRepository;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFeedbackUpdated(FeedbackUpdatedEvent event) {
        // 🔄 Force Hibernate to refresh and clear stale cached data
        entityManager.clear();

        // ✅ Fetch the latest feedback with comments AFTER transaction commits
        Feedback feedback = feedbackRepository.findById(event.getFeedbackId())
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        FeedbackResponseDTO responseDTO = new FeedbackResponseDTO(feedback);

        // 🔥 Debugging: Check if the new comment exists
        try {
            System.out.println("🔥 WebSocket Broadcasting: " + objectMapper.writeValueAsString(responseDTO));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // ✅ Send WebSocket Update
        messagingTemplate.convertAndSend("/topic/feedback/" + event.getFeedbackId(), responseDTO);
    }
}

