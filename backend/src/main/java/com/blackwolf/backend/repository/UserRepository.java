package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmailAndCompanyId(String email, String companyId);
    List<User> findByCompanyId(String companyId);
    Optional<User> findByEmailAndCompanyIdIsNull(String email);
}
