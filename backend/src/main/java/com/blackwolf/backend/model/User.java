package com.blackwolf.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    private String id;

    private String email;
    private String fullName;

    @JsonIgnore
    private String password;
    private String role;
    private String companyId;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
