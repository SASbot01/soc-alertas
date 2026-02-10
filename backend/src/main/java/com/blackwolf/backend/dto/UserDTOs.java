package com.blackwolf.backend.dto;

import lombok.Data;

public class UserDTOs {

    @Data
    public static class CreateUserRequest {
        private String email;
        private String fullName;
        private String role;
        private String password;
    }

    @Data
    public static class UpdateUserRequest {
        private String fullName;
        private String role;
        private Boolean isActive;
    }
}
