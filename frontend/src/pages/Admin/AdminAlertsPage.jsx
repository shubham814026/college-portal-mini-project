import { useState } from 'react';
import api from '../../api/client';
import { PageHeader, useToast } from '../../components/UI';

export default function AdminAlertsPage() {
  const [message, setMessage] = useState('');
  const [sending, setSending] = useState(false);
  const { showToast, ToastView } = useToast();

  const handleSend = async (e) => {
    e.preventDefault();
    setSending(true);
    try {
      await api.sendAlert(message);
      showToast('Alert sent to all online users!');
      setMessage('');
    } catch (err) { showToast(err.message, 'error'); }
    setSending(false);
  };

  return (
    <>
      <PageHeader title="🚨 Send Alert" subtitle="Broadcast an urgent alert to all online students" />
      <div className="page-body">
        <div className="glass-card" style={{ maxWidth: 600, padding: 32 }}>
          <form onSubmit={handleSend}>
            <div style={{ marginBottom: 20 }}>
              <label className="label">Alert Message</label>
              <textarea className="textarea" value={message} onChange={e => setMessage(e.target.value)}
                required rows={4} placeholder="Type your urgent alert..." />
            </div>
            <button type="submit" className="btn btn-danger" disabled={sending} style={{ width: '100%', padding: '12px' }}>
              {sending ? '⏳ Sending...' : '🚨 Broadcast Alert'}
            </button>
          </form>
          <p style={{ marginTop: 16, fontSize: 12, color: 'var(--text-muted)' }}>
            ⚠ This sends a UDP multicast alert. All connected students will see a popup notification.
          </p>
        </div>
      </div>
      <ToastView />
    </>
  );
}
