package com.feedback.feedback_management.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.feedback_management.dto.CommentRequestDTO;
import com.feedback.feedback_management.dto.FeedbackHistoryResponseDTO;
import com.feedback.feedback_management.dto.FeedbackRequestDTO;
import com.feedback.feedback_management.dto.FeedbackResponseDTO;
import com.feedback.feedback_management.entity.Feedback;
import com.feedback.feedback_management.entity.FeedbackHistory;
import com.feedback.feedback_management.entity.User;
import com.feedback.feedback_management.enums.*;
import com.feedback.feedback_management.service.FeedbackService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final SimpMessagingTemplate messagingTemplate;

    public FeedbackController(FeedbackService feedbackService, SimpMessagingTemplate messagingTemplate) {
        this.feedbackService = feedbackService;
        this.messagingTemplate = messagingTemplate;
    }

    @PreAuthorize("hasAnyRole('USER', 'APPROVER', 'ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<FeedbackResponseDTO> submitFeedback(@RequestBody FeedbackRequestDTO feedbackRequestDTO) {
        try {
            FeedbackResponseDTO responseDTO = feedbackService.submitFeedback(feedbackRequestDTO);
            return ResponseEntity.ok().body(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        return  ResponseEntity.ok(feedbackService.getAllFeedback());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feedback> getFeedbackById(@PathVariable long id) {
        return feedbackService.getFeedbackById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<?> updateFeedback(@PathVariable long id, @RequestBody Feedback updatedFeedback, @RequestParam Long changedById) {
        try {
            Feedback savedFeedback = feedbackService.updateFeedback(id, updatedFeedback, changedById);
            return ResponseEntity.ok(savedFeedback);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<FeedbackHistoryResponseDTO>> getFeedbackHistory(
            @PathVariable Long id,
            @RequestParam(required = false) String changedBy,
            @RequestParam(required = false) FeedbackStatus previousStatus,
            @RequestParam(required = false) FeedbackStatus newStatus,
            @RequestParam(required = false) FeedbackPriority previousPriority,
            @RequestParam(required = false) FeedbackPriority newPriority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "changeTimestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {

        List<FeedbackHistoryResponseDTO> history = feedbackService.getFeedbackHistory(
                id, changedBy, previousStatus, newStatus, previousPriority, newPriority, fromDate, toDate, sortBy, sortOrder);

        return ResponseEntity.ok(history);
    }

    @PreAuthorize("hasRole('APPROVER') or hasRole('ADMIN')")
    //Approve feedback
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveFeedabck(@PathVariable long id, @RequestParam long approverId) {
        try {
            FeedbackResponseDTO feedbackResponseDTO = feedbackService.approveFeedback(id, approverId);
            return ResponseEntity.ok(feedbackResponseDTO);
        } catch (RuntimeException e) {
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('APPROVER') or hasRole('ADMIN')")
    //Reject feedback
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectFeedback(@PathVariable long id, @RequestParam long approverId) {
        try {
            FeedbackResponseDTO feedbackResponseDTO = feedbackService.rejectFeedback(id, approverId);
            return ResponseEntity.ok(feedbackResponseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFeedback(@PathVariable long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.ok("Feedback deleted successfully");
    }

    @PutMapping("/{id}/assign-approvers")
    public ResponseEntity<?> assignApprovers(@PathVariable Long id, @RequestBody List<Long> approverIds) {
        Feedback feedback = feedbackService.assignApprovers(id, approverIds);
        return ResponseEntity.ok(new FeedbackResponseDTO(feedback));
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<String> addComment(@PathVariable long id,
                                             @RequestParam long userId,
                                             @RequestBody String comment) {
        feedbackService.addComment(id, userId, comment);
        return ResponseEntity.ok("Comment added successfully");
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("permitAll()") // ✅ Temporarily allow all users
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody CommentRequestDTO commentRequest) {
        if (commentRequest.getUserId() == null || commentRequest.getText().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("User ID and comment text are required.");
        }

        Feedback feedback = feedbackService.addComment(id, commentRequest.getUserId(), commentRequest.getText());

        FeedbackResponseDTO responseDTO = new FeedbackResponseDTO(feedback);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/feedbackResponseById/{id}")
    public ResponseEntity<FeedbackResponseDTO> getFeedbackResponseById(@PathVariable long id) {
        return feedbackService.getFeedbackResponseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
