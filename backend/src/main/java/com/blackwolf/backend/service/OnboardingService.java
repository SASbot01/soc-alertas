package com.blackwolf.backend.service;

import com.blackwolf.backend.dto.OnboardingDTOs.*;
import com.blackwolf.backend.model.*;
import com.blackwolf.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OnboardingService {

    private static final Logger log = LoggerFactory.getLogger(OnboardingService.class);

    @Autowired
    private OnboardingRequestRepository repository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlertConfigurationRepository alertConfigRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public OnboardingRequest submit(OnboardingFormRequest form) {
        OnboardingRequest req = new OnboardingRequest();
        req.setId(UUID.randomUUID().toString());
        req.setCompanyName(form.getCompanyName());
        req.setDomain(form.getDomain());
        req.setContactName(form.getContactName());
        req.setContactEmail(form.getContactEmail());
        req.setContactPhone(form.getContactPhone());
        req.setAcceptsTerms(form.isAcceptsTerms());
        req.setAcceptsDpa(form.isAcceptsDpa());
        req.setAcceptsNda(form.isAcceptsNda());
        req.setMonitorNetwork(form.isMonitorNetwork());
        req.setMonitorEndpoints(form.isMonitorEndpoints());
        req.setMonitorCloud(form.isMonitorCloud());
        req.setMonitorEmail(form.isMonitorEmail());
        req.setNumServers(form.getNumServers());
        req.setNumEndpoints(form.getNumEndpoints());
        req.setNumLocations(form.getNumLocations());
        req.setCurrentSecurityTools(form.getCurrentSecurityTools());
        req.setAdditionalNotes(form.getAdditionalNotes());
        req.setAlertEmail(form.getAlertEmail());
        req.setAlertSlackWebhook(form.getAlertSlackWebhook());
        req.setPreferredSla(form.getPreferredSla());
        req.setStatus("pending");
        req.setCreatedAt(LocalDateTime.now());
        return repository.save(req);
    }

    public List<OnboardingRequest> listAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public List<OnboardingRequest> listPending() {
        return repository.findByStatus("pending");
    }

    @Transactional
    public ReviewResponse review(String id, String status, String reviewedBy) {
        OnboardingRequest req = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Onboarding request not found"));
        req.setStatus(status);
        req.setReviewedBy(reviewedBy);
        req.setReviewedAt(LocalDateTime.now());
        repository.save(req);

        ReviewResponse response = new ReviewResponse();
        response.setId(req.getId());
        response.setStatus(status);
        response.setCompanyName(req.getCompanyName());
        response.setDomain(req.getDomain());
        response.setAdminEmail(req.getContactEmail());

        // When approved, provision the company automatically
        if ("approved".equals(status)) {
            ProvisionResult result = provisionCompany(req);
            if (result != null) {
                response.setCompanyId(result.companyId);
                response.setApiKey(result.apiKey);
                response.setTempPassword(result.tempPassword);
            }
        }

        return response;
    }

    private record ProvisionResult(String companyId, String apiKey, String tempPassword) {}

    private ProvisionResult provisionCompany(OnboardingRequest req) {
        // Check if company already exists
        if (companyRepository.findByDomain(req.getDomain()).isPresent()) {
            log.warn("Company with domain {} already exists, skipping provisioning", req.getDomain());
            return null;
        }

        // 1. Create company
        String companyId = UUID.randomUUID().toString();
        String apiKey = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

        String plan = switch (req.getPreferredSla() != null ? req.getPreferredSla() : "standard") {
            case "enterprise" -> "enterprise";
            case "premium" -> "premium";
            case "basic" -> "basic";
            default -> "standard";
        };

        Company company = new Company();
        company.setId(companyId);
        company.setCompanyName(req.getCompanyName());
        company.setDomain(req.getDomain());
        company.setContactEmail(req.getContactEmail());
        company.setContactPhone(req.getContactPhone());
        company.setPlan(plan);
        company.setApiKey(apiKey);
        company.setStatus("active");
        company.setRegisteredAt(LocalDateTime.now());
        company.setTotalThreats(0L);
        companyRepository.save(company);

        log.info("Company provisioned: {} ({}), API Key: {}", req.getCompanyName(), req.getDomain(), apiKey);

        // 2. Create admin user with a temporary password (contact email as login)
        String tempPassword = UUID.randomUUID().toString().substring(0, 12);

        User admin = new User();
        admin.setId(UUID.randomUUID().toString());
        admin.setEmail(req.getContactEmail());
        admin.setFullName(req.getContactName());
        admin.setPassword(passwordEncoder.encode(tempPassword));
        admin.setRole("admin");
        admin.setCompanyId(companyId);
        admin.setActive(true);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        log.info("Admin user created: {} (temp password: {})", req.getContactEmail(), tempPassword);

        // 3. Setup alert configurations from onboarding data
        if (req.getAlertEmail() != null && !req.getAlertEmail().isBlank()) {
            AlertConfiguration emailAlert = new AlertConfiguration();
            emailAlert.setId(UUID.randomUUID().toString());
            emailAlert.setCompanyId(companyId);
            emailAlert.setAlertType("email");
            emailAlert.setDestination(req.getAlertEmail());
            emailAlert.setMinSeverity(5);
            emailAlert.setActive(true);
            emailAlert.setCreatedAt(LocalDateTime.now());
            alertConfigRepository.save(emailAlert);
            log.info("Email alert configured: {}", req.getAlertEmail());
        }

        if (req.getAlertSlackWebhook() != null && !req.getAlertSlackWebhook().isBlank()) {
            AlertConfiguration slackAlert = new AlertConfiguration();
            slackAlert.setId(UUID.randomUUID().toString());
            slackAlert.setCompanyId(companyId);
            slackAlert.setAlertType("slack");
            slackAlert.setDestination(req.getAlertSlackWebhook());
            slackAlert.setMinSeverity(7);
            slackAlert.setActive(true);
            slackAlert.setCreatedAt(LocalDateTime.now());
            alertConfigRepository.save(slackAlert);
            log.info("Slack alert configured");
        }

        log.info("=== Provisioning complete for {} ===", req.getCompanyName());
        log.info("  Domain: {}", req.getDomain());
        log.info("  API Key: {}", apiKey);
        log.info("  Admin login: {} / {}", req.getContactEmail(), tempPassword);

        return new ProvisionResult(companyId, apiKey, tempPassword);
    }
}
