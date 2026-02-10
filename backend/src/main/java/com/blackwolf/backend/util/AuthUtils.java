package com.blackwolf.backend.util;

import com.blackwolf.backend.model.User;
import com.blackwolf.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

    @Autowired
    private UserRepository userRepository;

    public String getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return (String) principal;
    }

    public String getCompanyId(Authentication authentication) {
        String userId = getUserId(authentication);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getCompanyId();
    }

    public User getUser(Authentication authentication) {
        String userId = getUserId(authentication);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public boolean isSuperAdmin(Authentication authentication) {
        User user = getUser(authentication);
        return "superadmin".equals(user.getRole());
    }
}
