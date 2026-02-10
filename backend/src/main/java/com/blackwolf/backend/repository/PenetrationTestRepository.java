package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.PenetrationTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PenetrationTestRepository extends JpaRepository<PenetrationTest, String> {
    List<PenetrationTest> findByCompanyId(String companyId);

    List<PenetrationTest> findByCompanyIdAndStatus(String companyId, String status);
}
