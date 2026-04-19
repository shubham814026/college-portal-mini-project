import { useState, useEffect } from 'react';
import api from '../../api/client';
import { PageHeader, Spinner, EmptyState, Modal, useToast } from '../../components/UI';

export default function ApprovalsPage() {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [previewFile, setPreviewFile] = useState(null);
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
                        <button className="btn btn-sm btn-success" onClick={() => handleAction(f.fileId, true)}>✅ Approve</button>
                        <button className="btn btn-sm btn-danger" onClick={() => handleAction(f.fileId, false)}>❌ Reject</button>
                        <button className="btn btn-sm btn-secondary" onClick={() => setPreviewFile(f)}>👁 Preview</button>
                        <a href={api.downloadUrl(f.fileId)} className="btn btn-sm btn-secondary" target="_blank" rel="noopener noreferrer">⬇ Download</a>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      <Modal open={!!previewFile} onClose={() => setPreviewFile(null)} title={previewFile?.originalName}>
        {previewFile && (() => {
          const name = (previewFile.originalName || '').toLowerCase();
          const isOffice = name.endsWith('.docx') || name.endsWith('.pptx') || name.endsWith('.xlsx');
          const inlineUrl = `${api.downloadUrl(previewFile.fileId)}&inline=true`;
          if (isOffice) {
            return (
              <div style={{ height: '65vh', display: 'flex', flexDirection: 'column', gap: 12 }}>
                <div style={{ fontSize: 13, color: 'var(--text-muted)', padding: '8px 0' }}>
                  ⚠️ Office documents (.docx, .pptx, .xlsx) cannot be previewed directly in the browser.
                </div>
                <a href={api.downloadUrl(previewFile.fileId)} className="btn btn-primary" target="_blank" rel="noopener noreferrer">⬇ Download to View</a>
              </div>
            );
          }
          return (
            <div style={{ height: '65vh' }}>
              <iframe src={inlineUrl} style={{ width: '100%', height: '100%', border: 'none', borderRadius: 8 }} title="Preview" />
            </div>
          );
        })()}
      </Modal>
      <ToastView />
    </>
  );
}
