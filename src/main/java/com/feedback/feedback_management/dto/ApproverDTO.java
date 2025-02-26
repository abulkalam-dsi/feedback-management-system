package com.feedback.feedback_management.dto;

import lombok.Getter;

@Getter
public class ApproverDTO {
    private Long id;
    private String name;

    public ApproverDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // ✅ Getters and setters
}

