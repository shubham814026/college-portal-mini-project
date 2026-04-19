import { useState, useEffect } from 'react';
import api from '../../api/client';
import { PageHeader, Spinner, EmptyState } from '../../components/UI';

export default function AdminLogsPage() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('');

  useEffect(() => {
    api.getLogs().then(setLogs).catch(console.error).finally(() => setLoading(false));
  }, []);

  if (loading) return <Spinner />;

  const filtered = filter
    ? logs.filter(l => l.action.toLowerCase().includes(filter.toLowerCase()) || l.username.toLowerCase().includes(filter.toLowerCase()))
    : logs;

  return (
    <>
      <PageHeader title="📋 System Logs" subtitle="Activity log for all system events" />
      <div className="page-body">
        <div style={{ marginBottom: 16 }}>
          <input className="input" style={{ maxWidth: 300 }} placeholder="Filter by action or username..."
            value={filter} onChange={e => setFilter(e.target.value)} />
        </div>
        {filtered.length === 0 ? (
          <EmptyState icon="📋" message="No log entries found." />
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>ID</th><th>User</th><th>Action</th><th>IP</th><th>Time</th></tr>
              </thead>
              <tbody>
                {filtered.map(l => (
                  <tr key={l.logId}>
                    <td style={{ color: 'var(--text-muted)' }}>#{l.logId}</td>
                    <td style={{ fontWeight: 600 }}>{l.username}</td>
                    <td><span className={`badge ${actionBadge(l.action)}`}>{l.action}</span></td>
                    <td style={{ fontFamily: 'monospace', fontSize: 12, color: 'var(--text-muted)' }}>{l.ipAddress || '—'}</td>
                    <td style={{ fontSize: 12, color: 'var(--text-muted)' }}>{new Date(l.loggedAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  );
}

function actionBadge(action) {
  if (action.includes('LOGIN')) return 'badge-green';
  if (action.includes('LOGOUT')) return 'badge-amber';
  if (action.includes('UPLOAD')) return 'badge-blue';
  if (action.includes('ALERT')) return 'badge-red';
  return 'badge-purple';
}
