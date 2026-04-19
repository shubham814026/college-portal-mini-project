import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/client';
import { useAuth } from '../../contexts/AuthContext';
import { PageHeader, Spinner } from '../../components/UI';

export default function DashboardPage() {
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.dashboard().then(setData).catch(console.error).finally(() => setLoading(false));
  }, []);

  if (loading) return <Spinner />;

  const greeting = () => {
    const h = new Date().getHours();
    if (h < 12) return 'Good Morning';
    if (h < 17) return 'Good Afternoon';
    return 'Good Evening';
  };

  return (
    <>
      <PageHeader title={`${greeting()}, ${user?.fullName || user?.username} 👋`}
        subtitle="Here's what's happening today" />
      <div className="page-body">
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 20, marginBottom: 28 }}>
          <StatCard icon="📢" label="Recent Notices" value={data?.notices?.length || 0} color="var(--accent-blue)" />
          <StatCard icon="📁" label="Recent Files" value={data?.files?.length || 0} color="var(--accent-green)" />
          <StatCard icon="📋" label="Recent Activity" value={data?.logs?.length || 0} color="var(--accent-purple)" />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
          {/* Recent Notices */}
          <div className="glass-card" style={{ padding: 20 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h3 style={{ fontSize: 16, fontWeight: 700 }}>📢 Latest Notices</h3>
              <Link to="/notices" className="btn btn-sm btn-secondary">View All</Link>
            </div>
            {data?.notices?.length === 0 ? (
              <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>No notices yet</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                {data?.notices?.slice(0, 5).map(n => (
                  <div key={n.noticeId} style={{
                    padding: '10px 14px', borderRadius: 8,
                    background: 'var(--bg-primary)', border: '1px solid var(--border)',
                  }} className="animate-fade-in">
                    <div style={{ fontSize: 14, fontWeight: 600 }}>{n.title}</div>
                    <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 4 }}>
                      by {n.postedByName} • {new Date(n.createdAt).toLocaleDateString()}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Recent Files */}
          <div className="glass-card" style={{ padding: 20 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h3 style={{ fontSize: 16, fontWeight: 700 }}>📁 Latest Files</h3>
              <Link to="/resources" className="btn btn-sm btn-secondary">View All</Link>
            </div>
            {data?.files?.length === 0 ? (
              <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>No files yet</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                {data?.files?.slice(0, 5).map(f => (
                  <div key={f.fileId} style={{
                    padding: '10px 14px', borderRadius: 8,
                    background: 'var(--bg-primary)', border: '1px solid var(--border)',
                    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                  }} className="animate-fade-in">
                    <div>
                      <div style={{ fontSize: 14, fontWeight: 600 }}>{f.originalName}</div>
                      <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 2 }}>
                        {f.fileSizeDisplay} • by {f.uploadedByName}
                      </div>
                    </div>
                    <span className="badge badge-blue">{f.fileType}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
}

function StatCard({ icon, label, value, color }) {
  return (
    <div className="glass-card" style={{ padding: '20px 24px', display: 'flex', alignItems: 'center', gap: 16 }}>
      <div style={{
        width: 48, height: 48, borderRadius: 12, display: 'flex',
        alignItems: 'center', justifyContent: 'center', fontSize: 22,
        background: `${color}18`, border: `1px solid ${color}30`,
      }}>{icon}</div>
      <div>
        <div style={{ fontSize: 28, fontWeight: 800, lineHeight: 1, color }}>{value}</div>
        <div style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>{label}</div>
      </div>
    </div>
  );
}
