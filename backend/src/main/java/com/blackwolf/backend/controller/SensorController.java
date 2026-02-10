package com.blackwolf.backend.controller;

import com.blackwolf.backend.dto.SensorDTOs.*;
import com.blackwolf.backend.model.Sensor;
import com.blackwolf.backend.repository.SensorRepository;
import com.blackwolf.backend.service.SensorService;
import com.blackwolf.backend.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sensors")
public class SensorController {

    @Autowired
    private SensorService sensorService;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private AuthUtils authUtils;

    @PostMapping("/upload")
    public ResponseEntity<SensorResponse> upload(@RequestBody SensorDataUpload upload) {
        return ResponseEntity.ok(sensorService.processUpload(upload));
    }

    @GetMapping
    public ResponseEntity<List<Sensor>> listSensors(Authentication authentication) {
        String companyId = authUtils.getCompanyId(authentication);
        List<Sensor> sensors = sensorRepository.findByCompanyId(companyId);
        return ResponseEntity.ok(sensors);
    }
}
