package com.feedback.feedback_management.dto;

import com.feedback.feedback_management.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDTO {
    private String name;
    private String email;
    private String password;
    private UserRole role;
}
