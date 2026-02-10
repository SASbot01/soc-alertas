package com.blackwolf.backend.dto;

import com.blackwolf.backend.model.User;
import lombok.Data;

public class AuthDTOs {

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
        private String companyDomain;
    }

    @Data
    public static class RegisterRequest {
        private String companyName;
        private String domain;
        private String contactEmail;
        private String contactPhone;
        private String plan;
    }

    @Data
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
    }

    @Data
    public static class AuthResponse {
        private String accessToken;
        private String tokenType = "Bearer";
        private User user;

        public AuthResponse(String accessToken, User user) {
            this.accessToken = accessToken;
            this.user = user;
        }
    }
}
