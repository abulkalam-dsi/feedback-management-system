package com.feedback.feedback_management.service;

import com.feedback.feedback_management.dto.FeedbackHistoryResponseDTO;
import com.feedback.feedback_management.dto.FeedbackRequestDTO;
import com.feedback.feedback_management.dto.FeedbackResponseDTO;
import com.feedback.feedback_management.entity.Feedback;
import com.feedback.feedback_management.entity.FeedbackHistory;
import com.feedback.feedback_management.entity.User;
import com.feedback.feedback_management.enums.FeedbackPriority;
import com.feedback.feedback_management.enums.FeedbackStatus;
import com.feedback.feedback_management.exception.*;
import com.feedback.feedback_management.repository.CommentRepository;
import com.feedback.feedback_management.repository.FeedbackHistoryRepository;
import com.feedback.feedback_management.repository.FeedbackRepository;
import com.feedback.feedback_management.repository.UserRepository;
import com.feedback.feedback_management.specification.FeedbackHistorySpecification;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
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

    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);

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
        logger.info("Received feedback submission request: {}", feedbackRequestDTO);

        if (feedbackRequestDTO.getCreatedBy() == null) {
            logger.error("Created Id is null in feedback submission request");
            throw new CustomException("Created Id is null", HttpStatus.BAD_REQUEST);
        }

        User createdByUser = userRepository.findById(feedbackRequestDTO.getCreatedBy())
                .orElseThrow(() -> {
                    logger.error("CreatedBy user not found: {}", feedbackRequestDTO.getCreatedBy());
                    return new CustomException("CreatedBy user not found", HttpStatus.NOT_FOUND);
                });

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
                    .orElseThrow(() -> {
                        logger.error("AssignedTo user not found: {}", feedbackRequestDTO.getAssignedTo());
                        return new CustomException("AssignedTo user not found", HttpStatus.NOT_FOUND);
                    });
            feedback.setAssignedTo(assignedToUser);
        }

        Feedback savedFeedback = feedbackRepository.save(feedback);
        logger.info("Feedback submitted successfully with ID: {}", savedFeedback.getId());

        return new FeedbackResponseDTO(savedFeedback);
    }

    public List<FeedbackResponseDTO> getAllFeedbacks(Long userId, String userRole) {
        logger.info("Fetching all feedbacks for userId: {} and role: {}", userId, userRole);
        List<Feedback> feedbackList;

        if (userRole.equals("ADMIN")) {
            feedbackList = feedbackRepository.findAll();
        } else {
            // Users can ONLY see feedback they created OR where they are an approver
            feedbackList = feedbackRepository.findByCreatedBy_IdOrApprovers_Id(userId, userId);
        }

        logger.info("Found {} feedback(s) for userId: {}", feedbackList.size(), userId);

        return feedbackList.stream()
                .map(feedback -> new FeedbackResponseDTO(feedback, commentRepository.findByFeedbackId(feedback.getId())))
                .collect(Collectors.toList());
    }

    public Optional<Feedback> getFeedbackById(Long id) {
        logger.info("Fetching feedback by ID: {}", id);
        return feedbackRepository.findById(id);
    }

    public List<Feedback> getFeedbackByStatus(FeedbackStatus status) {
        logger.info("Fetching feedbacks with status: {}", status);
        return feedbackRepository.findByStatus(status);
    }

    public Feedback updateFeedback(Long id, Feedback updatedFeedback, Long changedById) {
        logger.info("Updating feedback with ID: {}", id);
        User changeBy = userRepository.findById(changedById)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", changedById);
                    return new CustomException("User not found", HttpStatus.NOT_FOUND);
                });

        return feedbackRepository.findById(id).map(feedback -> {
            logger.debug("Saving history before updating feedback with ID: {}", id);
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

            Feedback savedFeedback = feedbackRepository.save(feedback);
            logger.info("Feedback updated successfully with ID: {}", savedFeedback.getId());

            return savedFeedback;
        }).orElseThrow(() -> {
            logger.error("Feedback not found with ID: {}", id);
            return new CustomException("Feedback not found", HttpStatus.NOT_FOUND);
        });
    }

    public List<FeedbackHistoryResponseDTO> getFeedbackHistory(Long feedbackId, String changedBy,
                                                               FeedbackStatus previousStatus, FeedbackStatus newStatus,
                                                               FeedbackPriority previousPriority, FeedbackPriority newPriority,
                                                               LocalDateTime fromDate, LocalDateTime toDate,
                                                               String sortBy, String sortOrder) {
        logger.info("Fetching feedback history for feedbackId: {}", feedbackId);
        //Apply filtering
        Specification<FeedbackHistory> specification = new FeedbackHistorySpecification(feedbackId, changedBy, previousStatus, newStatus, previousPriority, newPriority,fromDate, toDate
                );
        //Apply sorting
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        List<FeedbackHistory> historyList = feedbackHistoryRepository.findAll(specification, sort);

        logger.info("Found {} history entries for feedbackId: {}", historyList.size(), feedbackId);
        return historyList.stream()
                .map(FeedbackHistoryResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Approve Feedback (Updated)
    public FeedbackResponseDTO approveFeedback(long feedbackId, long approverId) {
        logger.info("Approving feedback with ID: {}", feedbackId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> {
                    logger.error("Feedback not found with ID: {}", feedbackId);
                    return new CustomException("Feedback not found", HttpStatus.NOT_FOUND);
                });

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> {
                    logger.error("Approver user not found with ID: {}", approverId);
                    return new CustomException("Approver user not found", HttpStatus.NOT_FOUND);
                });

        if (feedback.getStatus() != FeedbackStatus.AWAITING_APPROVAL) {
            logger.warn("Feedback already processed or not in approval stage. Feedback ID: {}", feedbackId);
            throw new CustomException("Feedback is already processed or not in approval stage.", HttpStatus.BAD_REQUEST);
        }

        if (!feedback.getApprovers().contains(approver)) {
            logger.warn("User is not an assigned approver for feedback ID: {}", feedbackId);
            throw new CustomException("User is not an assigned approver for this feedback.", HttpStatus.FORBIDDEN);
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
        logger.info("Feedback approved successfully. Feedback ID: {}", feedbackId);

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

    public FeedbackResponseDTO rejectFeedback(long feedbackId, long approverId) {
        logger.info("Rejecting feedback with ID: {}", feedbackId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> {
                    logger.error("Feedback not found with ID: {}", feedbackId);
                    return new CustomException("Feedback not found", HttpStatus.NOT_FOUND);
                });

        if (feedback.getStatus() != FeedbackStatus.AWAITING_APPROVAL) {
            logger.warn("Feedback already processed. Feedback ID: {}", feedbackId);
            throw new CustomException("Feedback is already processed.", HttpStatus.CONFLICT);
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> {
                    logger.error("Approver user not found with ID: {}", approverId);
                    return new CustomException("Approver user not found", HttpStatus.NOT_FOUND);
                });

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

        logger.info("Feedback rejected successfully. Feedback ID: {}", feedbackId);
        return new FeedbackResponseDTO(savedFeedback);
    }

    public void deleteFeedback(long feedbackId) {
        logger.info("Deleting feedback with ID: {}", feedbackId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> {
                    logger.error("Feedback not found with ID: {}", feedbackId);
                    return new CustomException("Feedback not found", HttpStatus.NOT_FOUND);
                });

        feedbackRepository.deleteById(feedbackId);
        logger.info("Feedback deleted successfully with ID: {}", feedbackId);
    }

    // Assign Approvers
    @Transactional
    public Feedback assignApprovers(long feedbackId, List<Long> approverIds) {
        logger.info("Assigning approvers to feedback with ID: {}", feedbackId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> {
                    logger.error("Feedback not found with ID: {}", feedbackId);
                    return new CustomException("Feedback not found", HttpStatus.NOT_FOUND);
                });

        if (feedback.getStatus() != FeedbackStatus.PENDING) {
            logger.warn("Cannot assign approvers. Feedback is already processed. Feedback ID: {}", feedbackId);
            throw new CustomException("Cannot assign approvers. Feedback is already processed.", HttpStatus.CONFLICT);
        }

        Set<User> approvers = new HashSet<>(userRepository.findAllById(approverIds));
        feedback.setApprovers(approvers);
        feedback.setStatus(FeedbackStatus.AWAITING_APPROVAL);

        User adminUser = getCurrentUser();
        Feedback updatedFeedback = feedbackRepository.save(feedback);

        FeedbackHistory history = new FeedbackHistory();
        history.setFeedback(feedback);
        history.setChangedBy(adminUser);
        history.setPreviousStatus(FeedbackStatus.PENDING);
        history.setNewStatus(FeedbackStatus.AWAITING_APPROVAL);
        history.setChangeTimestamp(LocalDateTime.now());
        history.setComment("Approvers Assigned: " +
                approvers.stream().map(User::getName).collect(Collectors.joining(", ")));
        feedbackHistoryRepository.save(history);

        logger.info("Approvers assigned successfully to feedback with ID: {}", feedbackId);
        return updatedFeedback;
    }

    @Transactional
    public Comment addComment(long feedbackId, long userId, String comment) {
        logger.info("Adding comment to feedback with ID: {}", feedbackId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> {
                    logger.error("Feedback not found with ID: {}", feedbackId);
                    return new CustomException("Feedback not found", HttpStatus.NOT_FOUND);
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new CustomException("User not found", HttpStatus.NOT_FOUND);
                });

        if (comment == null || comment.trim().isEmpty()) {
            logger.error("Empty comment provided for feedback ID: {}", feedbackId);
            throw new CustomException("Comment cannot be empty", HttpStatus.BAD_REQUEST);
        }

        // ✅ Publish event AFTER transaction commits
//        eventPublisher.publishEvent(new FeedbackUpdatedEvent(this, feedbackId));

        Comment saveComment = new Comment(feedback, user, comment);
        logger.info("Comment added to feedback with ID: {}", feedbackId);
        return commentRepository.save(saveComment);
    }

    public Optional<FeedbackResponseDTO> getFeedbackResponseById(long id) {
        logger.info("Fetching feedback response by ID: {}", id);

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
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
    }

}
