package com.feedback.feedback_management.controller;

import com.feedback.feedback_management.dto.FeedbackRequestDTO;
import com.feedback.feedback_management.dto.FeedbackResponseDTO;
import com.feedback.feedback_management.entity.Feedback;
import com.feedback.feedback_management.entity.User;
import com.feedback.feedback_management.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

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
}
