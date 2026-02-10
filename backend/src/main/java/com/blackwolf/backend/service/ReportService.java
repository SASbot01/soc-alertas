package com.blackwolf.backend.service;

import com.blackwolf.backend.model.*;
import com.blackwolf.backend.repository.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    @Autowired private CompanyRepository companyRepository;
    @Autowired private ThreatEventRepository threatEventRepository;
    @Autowired private SensorRepository sensorRepository;
    @Autowired private IncidentRepository incidentRepository;
    @Autowired private BlockedIPRepository blockedIPRepository;

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 22, Font.BOLD, new Color(30, 41, 59));
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(71, 85, 105));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(51, 65, 85));
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(100, 116, 139));
    private static final Color PRIMARY = new Color(14, 165, 233);
    private static final Color DARK_BG = new Color(30, 41, 59);

    public byte[] generateExecutiveReport(String companyId) throws Exception {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        List<ThreatEvent> threats = threatEventRepository.findByCompanyId(companyId);
        List<Sensor> sensors = sensorRepository.findByCompanyId(companyId);
        List<Incident> incidents = incidentRepository.findByCompanyId(companyId);
        List<BlockedIP> blockedIPs = blockedIPRepository.findByCompanyId(companyId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 50, 40);
        PdfWriter.getInstance(doc, out);
        doc.open();

        // Header
        Paragraph title = new Paragraph("BlackWolf SOC", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph sub = new Paragraph("Executive Security Report", SUBTITLE_FONT);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(5);
        doc.add(sub);

        Paragraph companyLine = new Paragraph(company.getCompanyName() + " (" + company.getDomain() + ")", BODY_FONT);
        companyLine.setAlignment(Element.ALIGN_CENTER);
        doc.add(companyLine);

        Paragraph dateLine = new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), SMALL_FONT);
        dateLine.setAlignment(Element.ALIGN_CENTER);
        dateLine.setSpacingAfter(20);
        doc.add(dateLine);

        doc.add(new Paragraph("\n"));

        // Summary Stats
        long threatsToday = threats.stream()
                .filter(t -> t.getTimestamp() != null && t.getTimestamp().isAfter(LocalDateTime.now().toLocalDate().atStartOfDay()))
                .count();
        long activeSensors = sensors.stream().filter(s -> "online".equals(s.getStatus())).count();
        long openIncidents = incidents.stream().filter(i -> !"resolved".equals(i.getStatus()) && !"closed".equals(i.getStatus())).count();

        PdfPTable statsTable = new PdfPTable(4);
        statsTable.setWidthPercentage(100);
        addStatCell(statsTable, "Total Threats", String.valueOf(threats.size()));
        addStatCell(statsTable, "Threats Today", String.valueOf(threatsToday));
        addStatCell(statsTable, "Active Sensors", activeSensors + "/" + sensors.size());
        addStatCell(statsTable, "Blocked IPs", String.valueOf(blockedIPs.size()));
        statsTable.setSpacingAfter(10);
        doc.add(statsTable);

        PdfPTable statsTable2 = new PdfPTable(4);
        statsTable2.setWidthPercentage(100);
        addStatCell(statsTable2, "Total Incidents", String.valueOf(incidents.size()));
        addStatCell(statsTable2, "Open Incidents", String.valueOf(openIncidents));
        long criticalThreats = threats.stream().filter(t -> t.getSeverity() != null && t.getSeverity() >= 8).count();
        addStatCell(statsTable2, "Critical Threats", String.valueOf(criticalThreats));
        long resolved = threats.stream().filter(t -> "resolved".equals(t.getStatus())).count();
        addStatCell(statsTable2, "Resolved", String.valueOf(resolved));
        statsTable2.setSpacingAfter(20);
        doc.add(statsTable2);

        // Threats Table
        doc.add(new Paragraph("Recent Threats", SUBTITLE_FONT));
        doc.add(new Paragraph("\n"));

        PdfPTable threatTable = new PdfPTable(new float[]{2f, 1f, 1.5f, 1.5f, 1f, 1.5f});
        threatTable.setWidthPercentage(100);
        addHeaderCell(threatTable, "Type");
        addHeaderCell(threatTable, "Severity");
        addHeaderCell(threatTable, "Source IP");
        addHeaderCell(threatTable, "Target IP");
        addHeaderCell(threatTable, "Status");
        addHeaderCell(threatTable, "Time");

        int count = 0;
        for (ThreatEvent t : threats) {
            if (count++ >= 30) break;
            addBodyCell(threatTable, t.getThreatType());
            addBodyCell(threatTable, t.getSeverity() != null ? t.getSeverity().toString() : "-");
            addBodyCell(threatTable, t.getSrcIp());
            addBodyCell(threatTable, t.getDstIp());
            addBodyCell(threatTable, t.getStatus());
            addBodyCell(threatTable, t.getTimestamp() != null ? t.getTimestamp().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) : "-");
        }
        doc.add(threatTable);

        // Incidents
        if (!incidents.isEmpty()) {
            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("Incidents", SUBTITLE_FONT));
            doc.add(new Paragraph("\n"));

            PdfPTable incTable = new PdfPTable(new float[]{3f, 1f, 1f, 1.5f, 1.5f});
            incTable.setWidthPercentage(100);
            addHeaderCell(incTable, "Title");
            addHeaderCell(incTable, "Severity");
            addHeaderCell(incTable, "Status");
            addHeaderCell(incTable, "Created");
            addHeaderCell(incTable, "SLA Deadline");

            for (Incident inc : incidents) {
                addBodyCell(incTable, inc.getTitle());
                addBodyCell(incTable, inc.getSeverity());
                addBodyCell(incTable, inc.getStatus());
                addBodyCell(incTable, inc.getCreatedAt() != null ? inc.getCreatedAt().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) : "-");
                addBodyCell(incTable, inc.getSlaDeadline() != null ? inc.getSlaDeadline().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) : "-");
            }
            doc.add(incTable);
        }

        // Footer
        doc.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph("CONFIDENTIAL - BlackWolf Defense SOC Platform", SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return out.toByteArray();
    }

    // CSV exports
    public String exportThreatsCSV(String companyId) {
        List<ThreatEvent> threats = threatEventRepository.findByCompanyId(companyId);
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Type,Severity,SourceIP,TargetIP,Port,Status,Timestamp,Description\n");
        for (ThreatEvent t : threats) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,\"%s\"\n",
                    t.getId(), t.getThreatType(), t.getSeverity(), t.getSrcIp(),
                    t.getDstIp(), t.getDstPort(), t.getStatus(), t.getTimestamp(),
                    t.getDescription() != null ? t.getDescription().replace("\"", "\"\"") : ""));
        }
        return csv.toString();
    }

    public String exportIncidentsCSV(String companyId) {
        List<Incident> incidents = incidentRepository.findByCompanyId(companyId);
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Title,Severity,Status,AssignedTo,Created,SLA Deadline,Resolved\n");
        for (Incident i : incidents) {
            csv.append(String.format("%s,\"%s\",%s,%s,%s,%s,%s,%s\n",
                    i.getId(), i.getTitle(), i.getSeverity(), i.getStatus(),
                    i.getAssignedTo() != null ? i.getAssignedTo() : "",
                    i.getCreatedAt(), i.getSlaDeadline(),
                    i.getResolvedAt() != null ? i.getResolvedAt() : ""));
        }
        return csv.toString();
    }

    private void addStatCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(new Color(51, 65, 85));
        cell.setBackgroundColor(DARK_BG);
        cell.setPadding(10);
        cell.addElement(new Paragraph(value, new Font(Font.HELVETICA, 18, Font.BOLD, PRIMARY)));
        cell.addElement(new Paragraph(label, SMALL_FONT));
        table.addCell(cell);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(DARK_BG);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", BODY_FONT));
        cell.setPadding(5);
        cell.setBorderColor(new Color(226, 232, 240));
        table.addCell(cell);
    }
}
