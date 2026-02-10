import api from './api';
import type {
  ThreatListResponse,
  Sensor,
  SecurityAudit,
  AuditDetailResponse,
  PenetrationTest,
  PentestDetailResponse,
  Vulnerability,
  VulnerabilityDashboard,
  SecurityCertification,
  SocMetricsResponse,
  ActivityLog,
  UserProfile,
  Incident,
  IncidentDetailResponse,
  AlertConfiguration,
  AlertHistory,
  OnboardingRequest,
} from '../types';

// ===== User Management =====

export const userService = {
  list: () => api.get<UserProfile[]>('/users').then(r => r.data),

  create: (data: { email: string; fullName: string; role: string; password: string }) =>
    api.post<UserProfile>('/users', data).then(r => r.data),

  update: (id: string, data: { fullName?: string; role?: string; isActive?: boolean }) =>
    api.put<UserProfile>(`/users/${id}`, data).then(r => r.data),

  delete: (id: string) =>
    api.delete(`/users/${id}`).then(r => r.data),
};

// ===== Superadmin =====

export const superAdminService = {
  getDashboard: () =>
    api.get('/superadmin/dashboard').then(r => r.data),

  getCompanies: () =>
    api.get('/superadmin/companies').then(r => r.data),

  getCompanyDashboard: (companyId: string) =>
    api.get(`/superadmin/companies/${companyId}/dashboard`).then(r => r.data),
};

// ===== Threats =====

export const threatService = {
  list: (params?: Record<string, string | number>) =>
    api.get<ThreatListResponse>('/threats', { params }).then(r => r.data),

  updateStatus: (id: string, status: string) =>
    api.patch(`/threats/${id}/status`, { status }).then(r => r.data),
};

// ===== Sensors =====

export const sensorService = {
  list: () => api.get<Sensor[]>('/sensors').then(r => r.data),
};

// ===== Audits =====

export const auditService = {
  list: () => api.get<SecurityAudit[]>('/audits').then(r => r.data),

  getDetail: (id: string) =>
    api.get<AuditDetailResponse>(`/audits/${id}`).then(r => r.data),

  create: (data: Record<string, unknown>) =>
    api.post<SecurityAudit>('/audits', data).then(r => r.data),

  updateStatus: (id: string, data: { status: string; executiveSummary?: string }) =>
    api.patch<SecurityAudit>(`/audits/${id}/status`, data).then(r => r.data),

  addFinding: (auditId: string, data: Record<string, unknown>) =>
    api.post(`/audits/${auditId}/findings`, data).then(r => r.data),

  updateFindingStatus: (findingId: string, status: string) =>
    api.patch(`/audits/findings/${findingId}/status`, { status }).then(r => r.data),

  getActivity: (id: string) =>
    api.get<ActivityLog[]>(`/audits/${id}/activity`).then(r => r.data),
};

// ===== Pentests =====

export const pentestService = {
  list: () => api.get<PenetrationTest[]>('/pentests').then(r => r.data),

  getDetail: (id: string) =>
    api.get<PentestDetailResponse>(`/pentests/${id}`).then(r => r.data),

  create: (data: Record<string, unknown>) =>
    api.post<PenetrationTest>('/pentests', data).then(r => r.data),

  updateStatus: (id: string, data: { status: string; executiveSummary?: string }) =>
    api.patch<PenetrationTest>(`/pentests/${id}/status`, data).then(r => r.data),

  addFinding: (pentestId: string, data: Record<string, unknown>) =>
    api.post(`/pentests/${pentestId}/findings`, data).then(r => r.data),
};

// ===== Vulnerabilities =====

export const vulnerabilityService = {
  list: () => api.get<Vulnerability[]>('/vulnerabilities').then(r => r.data),

  getDetail: (id: string) =>
    api.get<Vulnerability>(`/vulnerabilities/${id}`).then(r => r.data),

  create: (data: Record<string, unknown>) =>
    api.post<Vulnerability>('/vulnerabilities', data).then(r => r.data),

  updateStatus: (id: string, data: { status: string; remediationPlan?: string }) =>
    api.patch<Vulnerability>(`/vulnerabilities/${id}/status`, data).then(r => r.data),

  getDashboard: () =>
    api.get<VulnerabilityDashboard>('/vulnerabilities/dashboard').then(r => r.data),
};

// ===== Certifications =====

export const certificationService = {
  list: () => api.get<SecurityCertification[]>('/certifications').then(r => r.data),

  getDetail: (id: string) =>
    api.get<SecurityCertification>(`/certifications/${id}`).then(r => r.data),

  checkEligibility: (auditId: string) =>
    api.get<{ eligible: boolean }>(`/certifications/eligibility/${auditId}`).then(r => r.data),

  issue: (data: Record<string, unknown>) =>
    api.post<SecurityCertification>('/certifications', data).then(r => r.data),

  revoke: (id: string) =>
    api.patch<SecurityCertification>(`/certifications/${id}/revoke`).then(r => r.data),

  getMetrics: () =>
    api.get<SocMetricsResponse>('/certifications/metrics').then(r => r.data),
};

// ===== Incidents =====

export const incidentService = {
  list: () => api.get<Incident[]>('/incidents').then(r => r.data),

  getDetail: (id: string) =>
    api.get<IncidentDetailResponse>(`/incidents/${id}`).then(r => r.data),

  create: (data: { title: string; description: string; severity: string; assignedTo?: string; sourceThreatId?: string }) =>
    api.post<Incident>('/incidents', data).then(r => r.data),

  update: (id: string, data: { status?: string; assignedTo?: string; description?: string }) =>
    api.put<Incident>(`/incidents/${id}`, data).then(r => r.data),

  addTimeline: (id: string, data: { action: string; description: string }) =>
    api.post(`/incidents/${id}/timeline`, data).then(r => r.data),
};

// ===== Alerts =====

export const alertService = {
  listConfigs: () => api.get<AlertConfiguration[]>('/alerts/config').then(r => r.data),

  createConfig: (data: { alertType: string; destination: string; minSeverity?: number }) =>
    api.post<AlertConfiguration>('/alerts/config', data).then(r => r.data),

  updateConfig: (id: string, data: { destination?: string; minSeverity?: number; isActive?: boolean }) =>
    api.put<AlertConfiguration>(`/alerts/config/${id}`, data).then(r => r.data),

  deleteConfig: (id: string) =>
    api.delete(`/alerts/config/${id}`).then(r => r.data),

  getHistory: () => api.get<AlertHistory[]>('/alerts/history').then(r => r.data),
};

// ===== Onboarding =====

export const onboardingService = {
  submit: (data: Record<string, unknown>) =>
    api.post<OnboardingRequest>('/onboarding/submit', data).then(r => r.data),

  listAll: () => api.get<OnboardingRequest[]>('/onboarding/requests').then(r => r.data),

  listPending: () => api.get<OnboardingRequest[]>('/onboarding/requests/pending').then(r => r.data),

  review: (id: string, status: string) =>
    api.put(`/onboarding/requests/${id}/review`, { status }).then(r => r.data),
};

// ===== Reports =====

export const reportService = {
  downloadExecutivePDF: () =>
    api.get('/reports/executive', { responseType: 'blob' }).then(r => {
      const url = window.URL.createObjectURL(new Blob([r.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'blackwolf-executive-report.pdf');
      document.body.appendChild(link);
      link.click();
      link.remove();
    }),

  downloadThreatsCSV: () =>
    api.get('/reports/threats/csv', { responseType: 'blob' }).then(r => {
      const url = window.URL.createObjectURL(new Blob([r.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'threats-export.csv');
      document.body.appendChild(link);
      link.click();
      link.remove();
    }),

  downloadIncidentsCSV: () =>
    api.get('/reports/incidents/csv', { responseType: 'blob' }).then(r => {
      const url = window.URL.createObjectURL(new Blob([r.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'incidents-export.csv');
      document.body.appendChild(link);
      link.click();
      link.remove();
    }),
};
