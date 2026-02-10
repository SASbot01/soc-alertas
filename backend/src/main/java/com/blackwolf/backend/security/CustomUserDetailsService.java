package com.blackwolf.backend.security;

import com.blackwolf.backend.model.User;
import com.blackwolf.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        // Here we expect the ID or maybe email. For logging in with Email + Company, we
        // might need a composite lookup or store unique emails globally?
        // Let's assume ID is passed or we scan.
        // Actually, Security usually works with username.
        // We will store "id" as the username in the token subject.

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        java.util.List<org.springframework.security.core.GrantedAuthority> authorities = java.util.Collections
                .singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().toUpperCase()));

        return new org.springframework.security.core.userdetails.User(
                user.getId(),
                user.getPassword(),
                authorities);
    }
}
