package com.feedback.feedback_management.service;

import com.feedback.feedback_management.dto.FeedbackRequestDTO;
import com.feedback.feedback_management.dto.FeedbackResponseDTO;
import com.feedback.feedback_management.entity.Feedback;
import com.feedback.feedback_management.entity.User;
import com.feedback.feedback_management.enums.FeedbackStatus;
import com.feedback.feedback_management.repository.FeedbackRepository;
import com.feedback.feedback_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    public FeedbackResponseDTO submitFeedback(FeedbackRequestDTO feedbackRequestDTO) {
        if (feedbackRequestDTO.getCreatedBy() == null) {
            throw new RuntimeException("CreatedBy ID is null");
        }

        User createdByUser = userRepository.findById(feedbackRequestDTO.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("CreatedBy user not found"));

        Feedback feedback = new Feedback();
        feedback.setTitle(feedbackRequestDTO.getTitle());
        feedback.setDescription(feedbackRequestDTO.getDescription());
        feedback.setPriority(feedbackRequestDTO.getPriority());
        feedback.setCategory(feedbackRequestDTO.getCategory());
        feedback.setStatus(FeedbackStatus.PENDING);
        feedback.setCreatedBy(createdByUser);

        // If assignedToId is provided, validate the user
        if (feedbackRequestDTO.getAssignedTo() != null) {
            User assignedToUser = userRepository.findById(feedbackRequestDTO.getAssignedTo())
                    .orElseThrow(() -> new RuntimeException("AssignedTo user not found"));
            feedback.setAssignedTo(assignedToUser);
        }

        Feedback savedFeedback = feedbackRepository.save(feedback);
        return new FeedbackResponseDTO(savedFeedback);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    public Optional<Feedback> getFeedbackById(Long id) {
        return feedbackRepository.findById(id);
    }

    public List<Feedback> getFeedbackByStatus(FeedbackStatus status) {
        return feedbackRepository.findByStatus(status);
    }

    public Feedback updateFeedback(Long id, Feedback updatedFeedback) {
        return feedbackRepository.findById(id).map(feedback -> {
            feedback.setTitle(updatedFeedback.getTitle());
            feedback.setDescription(updatedFeedback.getDescription());
            feedback.setStatus(updatedFeedback.getStatus());
            feedback.setCategory(updatedFeedback.getCategory());
            feedback.setPriority(updatedFeedback.getPriority());
            return feedbackRepository.save(feedback);
        }).orElseThrow(() -> new RuntimeException("Feedback not found"));
    }
}
