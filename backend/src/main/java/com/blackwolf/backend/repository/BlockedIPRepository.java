package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.BlockedIP;
import com.blackwolf.backend.model.BlockedIPId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BlockedIPRepository extends JpaRepository<BlockedIP, BlockedIPId> {
    List<BlockedIP> findByCompanyId(String companyId);
}
