import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Landing from './pages/Landing';
import Dashboard from './pages/Dashboard';
import Sensors from './pages/Sensors';
import Threats from './pages/Threats';
import Audits from './pages/Audits';
import AuditDetail from './pages/AuditDetail';
import Pentests from './pages/Pentests';
import PentestDetail from './pages/PentestDetail';
import Vulnerabilities from './pages/Vulnerabilities';
import VulnerabilityDetail from './pages/VulnerabilityDetail';
import Certifications from './pages/Certifications';
import SocMetrics from './pages/SocMetrics';
import Incidents from './pages/Incidents';
import IncidentDetail from './pages/IncidentDetail';
import AlertConfig from './pages/AlertConfig';
import OnboardingRequests from './pages/OnboardingRequests';
import Settings from './pages/Settings';
import Users from './pages/Users';
import SuperAdminDashboard from './pages/SuperAdminDashboard';
import Companies from './pages/Companies';
import CompanyDetail from './pages/CompanyDetail';
import Layout from './components/Layout';
import { useAuth } from './context/AuthContext';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  if (loading) return <div className="h-screen flex items-center justify-center bg-slate-950 text-white">Loading...</div>;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <>{children}</>;
};

const DashboardRouter: React.FC = () => {
  const { user } = useAuth();
  if (user?.role === 'superadmin') return <SuperAdminDashboard />;
  return <Dashboard />;
};

const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/landing" element={<Landing />} />

      <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route index element={<DashboardRouter />} />
        <Route path="threats" element={<Threats />} />
        <Route path="sensors" element={<Sensors />} />
        <Route path="incidents" element={<Incidents />} />
        <Route path="incidents/:id" element={<IncidentDetail />} />
        <Route path="alerts" element={<AlertConfig />} />
        <Route path="audits" element={<Audits />} />
        <Route path="audits/:id" element={<AuditDetail />} />
        <Route path="pentests" element={<Pentests />} />
        <Route path="pentests/:id" element={<PentestDetail />} />
        <Route path="vulnerabilities" element={<Vulnerabilities />} />
        <Route path="vulnerabilities/:id" element={<VulnerabilityDetail />} />
        <Route path="certifications" element={<Certifications />} />
        <Route path="soc-metrics" element={<SocMetrics />} />
        <Route path="settings" element={<Settings />} />
        <Route path="users" element={<Users />} />
        <Route path="superadmin/companies" element={<Companies />} />
        <Route path="superadmin/company/:id" element={<CompanyDetail />} />
        <Route path="superadmin/onboarding" element={<OnboardingRequests />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default App;
