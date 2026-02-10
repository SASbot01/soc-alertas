package com.blackwolf.backend.controller;

import com.blackwolf.backend.dto.UserDTOs.*;
import com.blackwolf.backend.model.User;
import com.blackwolf.backend.service.UserService;
import com.blackwolf.backend.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<List<User>> listUsers(Authentication authentication) {
        String companyId = authUtils.getCompanyId(authentication);
        return ResponseEntity.ok(userService.listByCompany(companyId));
    }

    @PostMapping
    public ResponseEntity<User> createUser(Authentication authentication, @RequestBody CreateUserRequest request) {
        String companyId = authUtils.getCompanyId(authentication);
        return ResponseEntity.ok(userService.createUser(companyId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(Authentication authentication, @PathVariable String id, @RequestBody UpdateUserRequest request) {
        String companyId = authUtils.getCompanyId(authentication);
        return ResponseEntity.ok(userService.updateUser(id, companyId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(Authentication authentication, @PathVariable String id) {
        String companyId = authUtils.getCompanyId(authentication);
        userService.deleteUser(id, companyId);
        return ResponseEntity.ok().body(java.util.Map.of("message", "User deactivated"));
    }
}
