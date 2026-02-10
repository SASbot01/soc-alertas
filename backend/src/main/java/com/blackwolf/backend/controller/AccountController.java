package com.blackwolf.backend.controller;

import com.blackwolf.backend.dto.AuthDTOs.ChangePasswordRequest;
import com.blackwolf.backend.service.AuthService;
import com.blackwolf.backend.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthUtils authUtils;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest request) {
        String userId = authUtils.getUserId(authentication);
        authService.changePassword(userId, request);
        return ResponseEntity.ok().body(java.util.Map.of("message", "Password changed successfully"));
    }
}
