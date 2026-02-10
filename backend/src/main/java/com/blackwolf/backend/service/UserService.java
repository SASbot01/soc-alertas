package com.blackwolf.backend.service;

import com.blackwolf.backend.dto.UserDTOs.*;
import com.blackwolf.backend.model.User;
import com.blackwolf.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> listByCompany(String companyId) {
        return userRepository.findByCompanyId(companyId);
    }

    public User createUser(String companyId, CreateUserRequest request) {
        if (userRepository.findByEmailAndCompanyId(request.getEmail(), companyId).isPresent()) {
            throw new RuntimeException("User with this email already exists in this company");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole(request.getRole() != null ? request.getRole() : "user");
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCompanyId(companyId);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User updateUser(String userId, String companyId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!companyId.equals(user.getCompanyId())) {
            throw new RuntimeException("User does not belong to this company");
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getIsActive() != null) {
            user.setActive(request.getIsActive());
        }

        return userRepository.save(user);
    }

    public void deleteUser(String userId, String companyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!companyId.equals(user.getCompanyId())) {
            throw new RuntimeException("User does not belong to this company");
        }

        user.setActive(false);
        userRepository.save(user);
    }
}
