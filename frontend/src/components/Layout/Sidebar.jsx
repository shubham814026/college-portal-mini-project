import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

const studentNav = [
  { to: '/dashboard', icon: '📊', label: 'Dashboard' },
  { to: '/notices', icon: '📢', label: 'Notice Board' },
  { to: '/notice-groups', icon: '📋', label: 'Notice Groups' },
  { to: '/resources', icon: '📁', label: 'Resources' },
  { to: '/chat', icon: '💬', label: 'Chat' },
];

const adminNav = [
  { to: '/admin/alerts', icon: '🚨', label: 'Send Alert' },
  { to: '/admin/approvals', icon: '✅', label: 'Approvals' },
  { to: '/admin/logs', icon: '📋', label: 'System Logs' },
];

const facultyNav = [
  { to: '/admin/approvals', icon: '✅', label: 'Approvals' },
];

export default function Sidebar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const isAdmin = user?.role === 'ADMIN';
  const isFaculty = user?.role === 'FACULTY';

  return (
    <aside className="sidebar">
      {/* Brand */}
      <div style={{ padding: '20px 16px', borderBottom: '1px solid var(--border)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{
            width: 36, height: 36, borderRadius: 10,
            background: 'var(--gradient-1)', display: 'flex',
            alignItems: 'center', justifyContent: 'center', fontSize: 18
          }}>🎓</div>
          <div>
            <div style={{ fontWeight: 700, fontSize: 15 }}>College Portal</div>
            <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>Communication Hub</div>
          </div>
        </div>
      </div>

      {/* User */}
      <div style={{ padding: '16px', borderBottom: '1px solid var(--border)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div className="avatar avatar-sm">
            {user?.fullName?.[0] || user?.username?.[0] || '?'}
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: 13, fontWeight: 600, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {user?.fullName || user?.username}
            </div>
            <span className={`badge ${user?.role === 'ADMIN' ? 'badge-red' : user?.role === 'FACULTY' ? 'badge-purple' : 'badge-blue'}`}>
              {user?.role}
            </span>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav style={{ flex: 1, overflowY: 'auto', padding: '8px 0' }}>
        <div className="nav-section">
          <div className="nav-section-title">Main</div>
          {studentNav.map(item => (
            <NavLink key={item.to} to={item.to} className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <span className="icon">{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </div>

        {(isAdmin || isFaculty) && (
          <div className="nav-section" style={{ marginTop: 8 }}>
            <div className="nav-section-title">{isAdmin ? 'Admin' : 'Faculty'}</div>
            {(isAdmin ? adminNav : facultyNav).map(item => (
              <NavLink key={item.to} to={item.to} className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                <span className="icon">{item.icon}</span>
                {item.label}
              </NavLink>
            ))}
          </div>
        )}
      </nav>

      {/* Logout */}
      <div style={{ padding: '12px 16px', borderTop: '1px solid var(--border)' }}>
        <button onClick={handleLogout} className="btn btn-secondary" style={{ width: '100%' }}>
          🚪 Logout
        </button>
      </div>
    </aside>
  );
}
