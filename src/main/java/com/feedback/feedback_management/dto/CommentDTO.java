package com.feedback.feedback_management.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CommentDTO {
    private String userName;
    private String comment;
    private LocalDateTime createdAt;
}

