package com.feedback.feedback_management.dto;

import com.feedback.feedback_management.entity.User;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
public class CommentDTO {
    private String userName;
    private String comment;
    private LocalDateTime createdAt;

    public CommentDTO(User user, String comment, LocalDateTime createdAt) {
        this.userName = (user != null) ? user.getName() : "Unknown";
        this.comment = comment;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }
}

