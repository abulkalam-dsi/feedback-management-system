package com.feedback.feedback_management.dto;

import com.feedback.feedback_management.entity.User;
import com.feedback.feedback_management.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {
    private long id;
    private String name;
    private String email;
    private UserRole role;

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
    }
}
