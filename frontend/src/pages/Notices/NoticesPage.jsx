import { useState, useEffect } from 'react';
import api from '../../api/client';
import { useAuth } from '../../contexts/AuthContext';
import { PageHeader, Spinner, EmptyState, Modal, useToast } from '../../components/UI';

export default function NoticesPage() {
  const { user } = useAuth();
  const [notices, setNotices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingNotice, setEditingNotice] = useState(null);
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const { showToast, ToastView } = useToast();

  const canPost = user?.role === 'ADMIN' || user?.role === 'FACULTY';

  const loadNotices = () => {
    api.getNotices().then(setNotices).catch(console.error).finally(() => setLoading(false));
  };

  useEffect(() => { loadNotices(); }, []);

  const handlePost = async (e) => {
    e.preventDefault();
    try {
      if (editingNotice) {
        await api.updateNotice(editingNotice.noticeId, title, body);
        showToast('Notice updated successfully!');
      } else {
        await api.postNotice(title, body);
        showToast('Notice posted successfully!');
      }
      setShowModal(false);
      setTitle('');
      setBody('');
      setEditingNotice(null);
      loadNotices();
    } catch (err) {
      showToast(err.message, 'error');
    }
  };

  const handleEdit = (notice) => {
    setEditingNotice(notice);
    setTitle(notice.title);
    setBody(notice.body);
    setShowModal(true);
  };

  const handleDelete = async (noticeId) => {
    if (!window.confirm('Are you sure you want to delete this notice?')) return;
    try {
      await api.deleteNotice(noticeId);
      showToast('Notice deleted successfully');
      loadNotices();
    } catch (err) {
      showToast(err.message, 'error');
    }
  };

  const handleOpenNew = () => {
    setEditingNotice(null);
    setTitle('');
    setBody('');
    setShowModal(true);
  };

  if (loading) return <Spinner />;

  return (
    <>
      <PageHeader title="📢 Notice Board" subtitle="Official announcements from administration"
        actions={canPost && (
          <button className="btn btn-primary" onClick={handleOpenNew}>+ Post Notice</button>
        )} />
      <div className="page-body">
        {notices.length === 0 ? (
          <EmptyState icon="📢" message="No notices posted yet." />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            {notices.map(n => (
              <div key={n.noticeId} className="glass-card animate-fade-in" style={{ padding: 24 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <h3 style={{ fontSize: 18, fontWeight: 700 }}>{n.title}</h3>
                  <span className="badge badge-blue">{new Date(n.createdAt).toLocaleDateString()}</span>
                </div>
                <p style={{ color: 'var(--text-secondary)', marginTop: 10, lineHeight: 1.7, fontSize: 14 }}>{n.body}</p>
                <div style={{ marginTop: 12, fontSize: 12, color: 'var(--text-muted)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span>Posted by <strong style={{ color: 'var(--text-secondary)' }}>{n.postedByName}</strong></span>
                  {canPost && (
                    <div style={{ display: 'flex', gap: 8 }}>
                      <button className="btn btn-sm btn-secondary" onClick={() => handleEdit(n)}>✏️ Edit</button>
                      <button className="btn btn-sm btn-danger" onClick={() => handleDelete(n.noticeId)}>🗑 Delete</button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <Modal open={showModal} onClose={() => setShowModal(false)} title={editingNotice ? "Edit Notice" : "Post Notice"}>
        <form onSubmit={handlePost}>
          <div style={{ marginBottom: 14 }}>
            <label className="label">Title</label>
            <input className="input" value={title} onChange={e => setTitle(e.target.value)} required />
          </div>
          <div style={{ marginBottom: 14 }}>
            <label className="label">Body</label>
            <textarea className="textarea" value={body} onChange={e => setBody(e.target.value)} required rows={4} />
          </div>
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Publish</button>
          </div>
        </form>
      </Modal>
      <ToastView />
    </>
  );
}
