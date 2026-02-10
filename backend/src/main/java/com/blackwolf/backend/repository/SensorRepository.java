package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SensorRepository extends JpaRepository<Sensor, String> {
    List<Sensor> findByCompanyId(String companyId);
}
