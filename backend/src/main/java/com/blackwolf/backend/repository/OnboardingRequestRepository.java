package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.OnboardingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OnboardingRequestRepository extends JpaRepository<OnboardingRequest, String> {
    List<OnboardingRequest> findByStatus(String status);
    List<OnboardingRequest> findAllByOrderByCreatedAtDesc();
}
