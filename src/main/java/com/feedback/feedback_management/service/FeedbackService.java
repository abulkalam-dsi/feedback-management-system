package com.feedback.feedback_management.service;

import com.feedback.feedback_management.dto.FeedbackHistoryResponseDTO;
import com.feedback.feedback_management.dto.FeedbackRequestDTO;
import com.feedback.feedback_management.dto.FeedbackResponseDTO;
import com.feedback.feedback_management.entity.Feedback;
import com.feedback.feedback_management.entity.FeedbackHistory;
import com.feedback.feedback_management.entity.User;
import com.feedback.feedback_management.enums.FeedbackPriority;
import com.feedback.feedback_management.enums.FeedbackStatus;
import com.feedback.feedback_management.repository.CommentRepository;
import com.feedback.feedback_management.repository.FeedbackHistoryRepository;
import com.feedback.feedback_management.repository.FeedbackRepository;
import com.feedback.feedback_management.repository.UserRepository;
import com.feedback.feedback_management.specification.FeedbackHistorySpecification;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import com.feedback.feedback_management.entity.Comment;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final FeedbackHistoryRepository feedbackHistoryRepository;
    private final CommentRepository commentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository,
                           UserRepository userRepository,
                           FeedbackHistoryRepository feedbackHistoryRepository,
                           ApplicationEventPublisher eventPublisher,
                           SimpMessagingTemplate messagingTemplate, CommentRepository commentRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
        this.feedbackHistoryRepository = feedbackHistoryRepository;
        this.eventPublisher = eventPublisher;
        this.messagingTemplate = messagingTemplate;
        this.commentRepository = commentRepository;
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

    public Feedback updateFeedback(Long id, Feedback updatedFeedback, Long changedById) {
        User changeBy = userRepository.findById(changedById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return feedbackRepository.findById(id).map(feedback -> {
            //Save history before updating
            FeedbackHistory history = new FeedbackHistory();
            history.setFeedback(feedback);
            history.setChangedBy(changeBy);
            history.setPreviousStatus(feedback.getStatus());
            history.setNewStatus(updatedFeedback.getStatus());
            history.setPreviousPriority(feedback.getPriority());
            history.setNewPriority(updatedFeedback.getPriority());
            feedbackHistoryRepository.save(history);

            //Update feedback
            feedback.setTitle(updatedFeedback.getTitle());
            feedback.setDescription(updatedFeedback.getDescription());
            feedback.setStatus(updatedFeedback.getStatus());
            feedback.setCategory(updatedFeedback.getCategory());
            feedback.setPriority(updatedFeedback.getPriority());
            return feedbackRepository.save(feedback);
        }).orElseThrow(() -> new RuntimeException("Feedback not found"));
    }

    public List<FeedbackHistoryResponseDTO> getFeedbackHistory(Long feedbackId, String changedBy,
                                                               FeedbackStatus previousStatus, FeedbackStatus newStatus,
                                                               FeedbackPriority previousPriority, FeedbackPriority newPriority,
                                                               LocalDateTime fromDate, LocalDateTime toDate,
                                                               String sortBy, String sortOrder) {
        //Apply filtering
        Specification<FeedbackHistory> specification = new FeedbackHistorySpecification(feedbackId, changedBy, previousStatus, newStatus, previousPriority, newPriority,fromDate, toDate
                );
        //Apply sorting
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        List<FeedbackHistory> historyList = feedbackHistoryRepository.findAll(specification, sort);
        return historyList.stream()
                .map(FeedbackHistoryResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Approve Feedback (Updated)
    public FeedbackResponseDTO approveFeedback(long feedbackId, long approverId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver user not found"));

        if (feedback.getStatus() != FeedbackStatus.AWAITING_APPROVAL) {
            throw new RuntimeException("Feedback is already processed or not in approval stage.");
        }

        if (!feedback.getApprovers().contains(approver)) {
            throw new RuntimeException("User is not an assigned approver for this feedback.");
        }

        // Track who has approved
        feedback.approveBy(approver);

        // Check if all approvers have approved
        if (feedback.isFullyApproved()) {
            feedback.setStatus(FeedbackStatus.APPROVED);
            feedback.setApprovalDate(LocalDateTime.now());
        }

        // Save feedback update
        Feedback savedFeedback = feedbackRepository.save(feedback);

        // Log approval in history
        FeedbackHistory history = new FeedbackHistory();
        history.setFeedback(feedback);
        history.setChangedBy(approver);
        history.setPreviousStatus(FeedbackStatus.AWAITING_APPROVAL);
        history.setNewStatus(savedFeedback.getStatus()); // Could be `APPROVED` or still `AWAITING_APPROVAL`
        history.setChangeTimestamp(LocalDateTime.now());
        history.setComment("Approved by " + approver.getName());
        feedbackHistoryRepository.save(history);

        return new FeedbackResponseDTO(savedFeedback);
    }

    // Reject Feedback (Updated)
    public FeedbackResponseDTO rejectFeedback(long feedbackId, long approverId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        if (feedback.getStatus() != FeedbackStatus.AWAITING_APPROVAL) {
            throw new RuntimeException("Feedback is already processed.");
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver user not found"));

        if (!feedback.getApprovers().contains(approver)) {
            throw new RuntimeException("User is not an assigned approver for this feedback.");
        }

        feedback.setStatus(FeedbackStatus.REJECTED);
        feedback.setApprovalDate(LocalDateTime.now());
        Feedback savedFeedback = feedbackRepository.save(feedback);

        // Log rejection in history
        FeedbackHistory history = new FeedbackHistory();
        history.setFeedback(feedback);
        history.setChangedBy(approver);
        history.setPreviousStatus(FeedbackStatus.AWAITING_APPROVAL);
        history.setNewStatus(FeedbackStatus.REJECTED);
        history.setChangeTimestamp(LocalDateTime.now());
        history.setComment("Rejected by " + approver.getName());
        feedbackHistoryRepository.save(history);

        return new FeedbackResponseDTO(savedFeedback);
    }

    public void deleteFeedback(long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        feedbackRepository.deleteById(feedbackId);
    }

    // Assign Approvers
    @Transactional
    public Feedback assignApprovers(long feedbackId, List<Long> approverIds) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        if (feedback.getStatus() != FeedbackStatus.PENDING) {
            throw new RuntimeException("Cannot assign approvers. Feedback is already processed.");
        }

        Set<User> approvers = new HashSet<>(userRepository.findAllById(approverIds));
        feedback.setApprovers(approvers);
        feedback.setStatus(FeedbackStatus.AWAITING_APPROVAL); // ✅ Update status

        // ✅ Get current logged-in admin
        User adminUser = getCurrentUser();

        // ✅ Save feedback update
        Feedback updatedFeedback = feedbackRepository.save(feedback);

        // ✅ Log this in history
        FeedbackHistory history = new FeedbackHistory();
        history.setFeedback(feedback);
        history.setChangedBy(adminUser); // ✅ Set the logged-in admin
        history.setPreviousStatus(FeedbackStatus.PENDING);
        history.setNewStatus(FeedbackStatus.AWAITING_APPROVAL);
        history.setChangeTimestamp(LocalDateTime.now());
        history.setComment("Approvers Assigned: " +
                approvers.stream().map(User::getName).collect(Collectors.joining(", "))); // ✅ Log assigned approvers

        feedbackHistoryRepository.save(history);

        return updatedFeedback;
    }

    @Transactional
    public Comment addComment(long feedbackId, long userId, String comment) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (comment == null || comment.trim().isEmpty()) {
            throw new RuntimeException("Comment cannot be empty");
        }

        // ✅ Publish event AFTER transaction commits
//        eventPublisher.publishEvent(new FeedbackUpdatedEvent(this, feedbackId));

        Comment saveComment = new Comment(feedback, user, comment);
        return commentRepository.save(saveComment);
    }

    public Optional<FeedbackResponseDTO> getFeedbackResponseById(long id) {
        return feedbackRepository.findById(id)
                .map(feedback -> {
                    List<Comment> commentList = commentRepository.findByFeedbackId(id);
                    if (commentList == null) commentList = new ArrayList<>();
                    return new FeedbackResponseDTO(feedback, commentList);
                });
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
