import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import AppLayout from './components/Layout/AppLayout';
import LoginPage from './pages/Login/LoginPage';
import DashboardPage from './pages/Dashboard/DashboardPage';
import NoticesPage from './pages/Notices/NoticesPage';
import NoticeGroupsPage from './pages/NoticeGroups/NoticeGroupsPage';
import ResourcesPage from './pages/Resources/ResourcesPage';
import ChatPage from './pages/Chat/ChatPage';
import AdminAlertsPage from './pages/Admin/AdminAlertsPage';
import ApprovalsPage from './pages/Admin/ApprovalsPage';
import AdminLogsPage from './pages/Admin/AdminLogsPage';
import { Spinner } from './components/UI';

function ProtectedRoute({ children, roles }) {
  const { user, loading } = useAuth();
  if (loading) return <Spinner />;
  if (!user) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/dashboard" replace />;
  return children;
}

function PublicRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <Spinner />;
  if (user) return <Navigate to="/dashboard" replace />;
  return children;
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
          <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/notices" element={<NoticesPage />} />
            <Route path="/notice-groups" element={<NoticeGroupsPage />} />
            <Route path="/resources" element={<ResourcesPage />} />
            <Route path="/chat" element={<ChatPage />} />
            <Route path="/admin/alerts" element={<ProtectedRoute roles={['ADMIN']}><AdminAlertsPage /></ProtectedRoute>} />
            <Route path="/admin/approvals" element={<ProtectedRoute roles={['ADMIN', 'FACULTY']}><ApprovalsPage /></ProtectedRoute>} />
            <Route path="/admin/logs" element={<ProtectedRoute roles={['ADMIN']}><AdminLogsPage /></ProtectedRoute>} />
          </Route>
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
