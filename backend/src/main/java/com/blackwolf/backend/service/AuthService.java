package com.blackwolf.backend.service;

import com.blackwolf.backend.dto.AuthDTOs.*;
import com.blackwolf.backend.model.Company;
import com.blackwolf.backend.model.User;
import com.blackwolf.backend.repository.CompanyRepository;
import com.blackwolf.backend.repository.UserRepository;
import com.blackwolf.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public AuthResponse login(LoginRequest loginRequest) {
        User user;

        // Superadmin login: no company domain
        if (loginRequest.getCompanyDomain() == null || loginRequest.getCompanyDomain().isBlank()) {
            user = userRepository.findByEmailAndCompanyIdIsNull(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!"superadmin".equals(user.getRole())) {
                throw new RuntimeException("Domain is required for non-superadmin users");
            }
        } else {
            // Regular login: find company first
            Company company = companyRepository.findByDomain(loginRequest.getCompanyDomain())
                    .orElseThrow(() -> new RuntimeException("Company not found"));
            user = userRepository.findByEmailAndCompanyId(loginRequest.getEmail(), company.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getId(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return new AuthResponse(jwt, user);
    }

    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public Company register(RegisterRequest registerRequest) {
        if (companyRepository.findByDomain(registerRequest.getDomain()).isPresent()) {
            throw new RuntimeException("Company already registered");
        }

        Company company = new Company();
        company.setId(UUID.randomUUID().toString());
        company.setCompanyName(registerRequest.getCompanyName());
        company.setDomain(registerRequest.getDomain());
        company.setContactEmail(registerRequest.getContactEmail());
        company.setContactPhone(registerRequest.getContactPhone());
        company.setPlan(registerRequest.getPlan());
        company.setApiKey(UUID.randomUUID().toString()); // Generate API Key
        company.setStatus("active");
        company.setRegisteredAt(LocalDateTime.now());
        company.setTotalThreats(0L);

        company = companyRepository.save(company);

        // Create Admin User
        User admin = new User();
        admin.setId(UUID.randomUUID().toString());
        admin.setEmail(registerRequest.getContactEmail());
        admin.setFullName("Admin");
        admin.setCompanyId(company.getId());
        admin.setRole("admin");
        admin.setActive(true);
        admin.setCreatedAt(LocalDateTime.now());

        // Default password
        String defaultPassword = "admin"; // Should be changed or generated
        admin.setPassword(passwordEncoder.encode(defaultPassword));

        userRepository.save(admin);

        return company;
    }
}
