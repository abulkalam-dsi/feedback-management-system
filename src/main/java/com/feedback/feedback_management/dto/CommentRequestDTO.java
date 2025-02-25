package com.feedback.feedback_management.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDTO {
    private Long userId;
    private String text;
}
