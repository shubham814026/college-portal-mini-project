import { useState, useEffect } from 'react';
import api from '../../api/client';
import { PageHeader, Spinner, EmptyState, useToast } from '../../components/UI';

export default function ApprovalsPage() {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const { showToast, ToastView } = useToast();

  const loadPending = () => {
    api.getPendingFiles().then(setFiles).catch(console.error).finally(() => setLoading(false));
  };

  useEffect(() => { loadPending(); }, []);

  const handleAction = async (fileId, approve) => {
    try {
      if (approve) await api.approveFile(fileId);
      else await api.rejectFile(fileId);
      showToast(approve ? 'File approved!' : 'File rejected');
      loadPending();
    } catch (err) { showToast(err.message, 'error'); }
  };

  if (loading) return <Spinner />;

  return (
    <>
      <PageHeader title="✅ File Approvals" subtitle="Review and approve student file uploads" />
      <div className="page-body">
        {files.length === 0 ? (
          <EmptyState icon="✅" message="No files pending approval." />
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>File</th><th>Type</th><th>Size</th><th>Subject</th><th>Branch</th><th>Uploaded By</th><th>Date</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {files.map(f => (
                  <tr key={f.fileId} className="animate-fade-in">
                    <td style={{ fontWeight: 600 }}>{f.originalName}</td>
                    <td><span className="badge badge-blue">{f.fileType}</span></td>
                    <td>{f.fileSizeDisplay}</td>
                    <td>{f.subjectTag || '—'}</td>
                    <td>{f.branch || '—'}</td>
                    <td>{f.uploadedByName}</td>
                    <td style={{ fontSize: 12, color: 'var(--text-muted)' }}>{new Date(f.uploadedAt).toLocaleDateString()}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 6 }}>
                        <button className="btn btn-sm btn-success" onClick={() => handleAction(f.fileId, true)}>✅</button>
                        <button className="btn btn-sm btn-danger" onClick={() => handleAction(f.fileId, false)}>❌</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      <ToastView />
    </>
  );
}
