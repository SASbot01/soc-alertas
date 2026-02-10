package com.blackwolf.backend.service;

import com.blackwolf.backend.dto.SensorDTOs.*;
import com.blackwolf.backend.model.*;
import com.blackwolf.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SensorService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ThreatEventRepository threatEventRepository;

    @Autowired
    private BlockedIPRepository blockedIPRepository;

    @Autowired
    private CorrelationService correlationService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private ThreatIntelService threatIntelService;

    @Transactional
    public SensorResponse processUpload(SensorDataUpload upload) {
        // Validate API Key / Company
        // In real world, we should check api_key against company_id
        // Assuming done or trusting passed company_id if authenticated separately (but
        // this endpoint might be public with api key)
        // Let's check company exists
        Optional<Company> company = companyRepository.findByApiKey(upload.getApi_key());
        if (company.isEmpty() || !company.get().getId().equals(upload.getCompany_id())) {
            throw new RuntimeException("Invalid API key or company mismatch");
        }

        // Update Sensor
        Sensor sensor = sensorRepository.findById(upload.getSensor_id()).orElse(new Sensor());
        sensor.setId(upload.getSensor_id());
        sensor.setCompanyId(upload.getCompany_id());
        sensor.setLastSeen(LocalDateTime.now());
        sensor.setStatus("online");
        if (sensor.getPacketsProcessed() == null)
            sensor.setPacketsProcessed(0L);
        if (sensor.getThreatsDetected() == null)
            sensor.setThreatsDetected(0L);

        if (upload.getPackets() != null) {
            sensor.setPacketsProcessed(sensor.getPacketsProcessed() + upload.getPackets().size());
        }
        if (upload.getThreats() != null) {
            sensor.setThreatsDetected(sensor.getThreatsDetected() + upload.getThreats().size());
        }
        sensorRepository.save(sensor);

        // Process Threats
        List<ThreatInfo> threats = upload.getThreats();
        if (threats != null) {
            for (ThreatInfo t : threats) {
                ThreatEvent event = new ThreatEvent();
                event.setId(UUID.randomUUID().toString());
                event.setCompanyId(upload.getCompany_id());
                event.setSensorId(upload.getSensor_id());
                event.setThreatType(t.getThreat_type());
                event.setSeverity(t.getSeverity());
                event.setSrcIp(t.getSrc_ip());
                event.setDstIp(t.getDst_ip());
                event.setDstPort(t.getDst_port());
                event.setTimestamp(LocalDateTime.now());
                event.setStatus("detected");
                event.setDescription(t.getDescription());

                threatEventRepository.save(event);

                // Correlation engine + alerts + threat intel
                try {
                    correlationService.evaluateThreat(event);
                    alertService.fireAlertsForThreat(event);
                    if (t.getSrc_ip() != null) {
                        threatIntelService.enrichIp(t.getSrc_ip());
                    }
                } catch (Exception e) {
                    // Don't fail the upload if enrichment/correlation fails
                }

                // Auto Block logic
                if (t.getSeverity() != null && t.getSeverity() >= 5) {
                    BlockedIPId id = new BlockedIPId();
                    id.setIp(t.getSrc_ip());
                    id.setCompanyId(upload.getCompany_id());

                    if (!blockedIPRepository.existsById(id)) {
                        BlockedIP blocked = new BlockedIP();
                        blocked.setIp(t.getSrc_ip());
                        blocked.setCompanyId(upload.getCompany_id());
                        blocked.setReason("Auto-block: " + t.getThreat_type());
                        blocked.setBlockedAt(LocalDateTime.now());
                        blocked.setExpiresAt(LocalDateTime.now().plusHours(24));
                        blockedIPRepository.save(blocked);
                    }
                }
            }
        }

        // Get Commands (Blocked IPs)
        List<BlockedIP> blockedIPs = blockedIPRepository.findByCompanyId(upload.getCompany_id());
        List<Command> commands = blockedIPs.stream()
                .map(b -> new Command("block_ip", b.getIp(), b.getReason()))
                .collect(Collectors.toList());

        SensorResponse response = new SensorResponse();
        response.setStatus("success");
        response.setProcessed_packets(upload.getPackets() != null ? upload.getPackets().size() : 0);
        response.setProcessed_threats(threats != null ? threats.size() : 0);
        response.setCommands(commands);
        return response;
    }
}
