import { useState, useEffect } from 'react';
import api from '../../api/client';
import { useAuth } from '../../contexts/AuthContext';
import { PageHeader, Spinner, EmptyState, Modal, useToast } from '../../components/UI';

export default function ResourcesPage() {
  const { user } = useAuth();
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showUpload, setShowUpload] = useState(false);
  const [filter, setFilter] = useState({ branch: '', year: '', semester: '', subject: '' });
  const [folderPath, setFolderPath] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [previewFile, setPreviewFile] = useState(null);
  const { showToast, ToastView } = useToast();

  // Upload form state
  const [uploadFiles, setUploadFiles] = useState([]);
  const [branch, setBranch] = useState('');
  const [year, setYear] = useState('');
  const [semester, setSemester] = useState('');
  const [subjectTag, setSubjectTag] = useState('');
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState({ current: 0, total: 0 });

  const loadFiles = () => {
    const params = {};
    if (filter.branch) params.branch = filter.branch;
    if (filter.year) params.year = filter.year;
    if (filter.semester) params.semester = filter.semester;
    if (filter.subject) params.subject = filter.subject;
    api.getFiles(Object.keys(params).length > 0 ? params : null)
      .then(setFiles).catch(console.error).finally(() => setLoading(false));
  };

  useEffect(() => { loadFiles(); }, [filter]);

  // Build folder tree from files
  const branches = [...new Set(files.map(f => f.branch).filter(Boolean))].sort();
  const years = filter.branch ? [...new Set(files.filter(f => f.branch === filter.branch).map(f => f.yearOfStudy).filter(Boolean))].sort() : [];
  const semesters = filter.year ? [...new Set(files.filter(f => f.branch === filter.branch && f.yearOfStudy == filter.year).map(f => f.semester).filter(Boolean))].sort() : [];
  const subjects = filter.semester ? [...new Set(files.filter(f => f.branch === filter.branch && f.yearOfStudy == filter.year && f.semester == filter.semester).map(f => f.subjectTag).filter(Boolean))].sort() : [];

  const handleUpload = async (e) => {
    e.preventDefault();
    if (uploadFiles.length === 0) return;

    setUploading(true);
    setUploadProgress({ current: 0, total: uploadFiles.length });

    let successCount = 0;
    let pendingCount = 0;
    let failCount = 0;

    for (let i = 0; i < uploadFiles.length; i++) {
      setUploadProgress({ current: i + 1, total: uploadFiles.length });
      const file = uploadFiles[i];
      const formData = new FormData();
      formData.append('file', file);
      formData.append('branch', branch);
      if (year) formData.append('year', year);
      if (semester) formData.append('semester', semester);
      if (subjectTag) formData.append('subjectTag', subjectTag);

      try {
        const res = await api.uploadFile(formData);
        if (res.approvalStatus === 'PENDING') pendingCount++;
        else successCount++;
      } catch (err) {
        failCount++;
      }
    }

    setUploading(false);
    const msgs = [];
    if (successCount > 0) msgs.push(`${successCount} file(s) uploaded!`);
    if (pendingCount > 0) msgs.push(`${pendingCount} file(s) awaiting approval.`);
    if (failCount > 0) msgs.push(`${failCount} file(s) failed.`);
    showToast(msgs.join(' '), failCount > 0 ? 'error' : 'success');

    setShowUpload(false);
    setUploadFiles([]);
    setBranch(''); setYear(''); setSemester(''); setSubjectTag('');
    loadFiles();
  };

  const handleDelete = async (fileId) => {
    if (!window.confirm('Are you sure you want to delete this file?')) return;
    try {
      await api.deleteFile(fileId);
      showToast('File deleted successfully');
      loadFiles();
    } catch (err) {
      showToast(err.message, 'error');
    }
  };

  const navigateFolder = (level, value) => {
    const newFilter = { branch: '', year: '', semester: '', subject: '' };
    if (level >= 1) newFilter.branch = level === 1 ? value : filter.branch;
    if (level >= 2) newFilter.year = level === 2 ? value : filter.year;
    if (level >= 3) newFilter.semester = level === 3 ? value : filter.semester;
    if (level >= 4) newFilter.subject = level === 4 ? value : filter.subject;
    setFilter(newFilter);
  };

  if (loading) return <Spinner />;

  return (
    <>
      <PageHeader title="📁 Resource Library" subtitle="Shared files organized by branch, year, and semester"
        actions={<button className="btn btn-primary" onClick={() => setShowUpload(true)}>⬆ Upload File</button>} />
      <div className="page-body">
        <div style={{ display: 'flex', gap: 24 }}>
          {/* Folder sidebar */}
          <div className="glass-card" style={{ width: 240, padding: 16, flexShrink: 0, alignSelf: 'flex-start' }}>
            <h4 style={{ fontSize: 13, fontWeight: 700, color: 'var(--text-muted)', marginBottom: 12, textTransform: 'uppercase', letterSpacing: 1 }}>Browse</h4>
            <div className="folder-tree">
              <div className={`folder-item ${!filter.branch ? 'active' : ''}`}
                onClick={() => setFilter({ branch: '', year: '', semester: '', subject: '' })}>
                📂 All Files
              </div>
              {branches.map(b => (
                <div key={b}>
                  <div className={`folder-item ${filter.branch === b && !filter.year ? 'active' : ''}`}
                    onClick={() => navigateFolder(1, b)} style={{ paddingLeft: 20 }}>
                    📁 {b}
                  </div>
                  {filter.branch === b && years.map(y => (
                    <div key={y}>
                      <div className={`folder-item ${filter.year == y && !filter.semester ? 'active' : ''}`}
                        onClick={() => navigateFolder(2, y)} style={{ paddingLeft: 36 }}>
                        📁 Year {y}
                      </div>
                      {filter.year == y && semesters.map(s => (
                        <div key={s}>
                          <div className={`folder-item ${filter.semester == s && !filter.subject ? 'active' : ''}`}
                            onClick={() => navigateFolder(3, s)} style={{ paddingLeft: 52 }}>
                            📁 Sem {s}
                          </div>
                          {filter.semester == s && subjects.map(sub => (
                            <div key={sub} className={`folder-item ${filter.subject === sub ? 'active' : ''}`}
                              onClick={() => navigateFolder(4, sub)} style={{ paddingLeft: 68 }}>
                              📁 {sub}
                            </div>
                          ))}
                        </div>
                      ))}
                    </div>
                  ))}
                </div>
              ))}
            </div>
          </div>

          {/* File list */}
          <div style={{ flex: 1 }}>
            {/* Breadcrumb */}
            <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 16, fontSize: 13, color: 'var(--text-muted)' }}>
              <span style={{ cursor: 'pointer', color: 'var(--accent-blue)' }}
                onClick={() => setFilter({ branch: '', year: '', semester: '', subject: '' })}>All</span>
              {filter.branch && <><span>/</span><span style={{ cursor: 'pointer', color: 'var(--accent-blue)' }}
                onClick={() => navigateFolder(1, filter.branch)}>{filter.branch}</span></>}
              {filter.year && <><span>/</span><span style={{ cursor: 'pointer', color: 'var(--accent-blue)' }}
                onClick={() => navigateFolder(2, filter.year)}>Year {filter.year}</span></>}
              {filter.semester && <><span>/</span><span style={{ cursor: 'pointer', color: 'var(--accent-blue)' }}
                onClick={() => navigateFolder(3, filter.semester)}>Sem {filter.semester}</span></>}
              {filter.subject && <><span>/</span><span>{filter.subject}</span></>}
            </div>

            <div style={{ marginBottom: 16 }}>
              <input type="text" className="input" placeholder="Search by name, subject, branch, year, semester..." value={searchQuery} onChange={e => setSearchQuery(e.target.value)} />
            </div>

            {files.filter(f => {
              if (!searchQuery) return true;
              const q = searchQuery.toLowerCase();
              return (f.originalName && f.originalName.toLowerCase().includes(q))
                || (f.subjectTag && f.subjectTag.toLowerCase().includes(q))
                || (f.branch && f.branch.toLowerCase().includes(q))
                || (f.uploadedByName && f.uploadedByName.toLowerCase().includes(q))
                || (f.yearOfStudy && String(f.yearOfStudy).includes(q))
                || (f.semester && String(f.semester).includes(q))
                || (f.fileType && f.fileType.toLowerCase().includes(q));
            }).length === 0 ? (
              <EmptyState icon="📂" message="No files found." />
            ) : (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>File Name</th>
                      <th>Type</th>
                      <th>Size</th>
                      <th>Subject</th>
                      <th>Uploaded By</th>
                      <th>Date</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {files.filter(f => {
                      if (!searchQuery) return true;
                      const q = searchQuery.toLowerCase();
                      return (f.originalName && f.originalName.toLowerCase().includes(q))
                        || (f.subjectTag && f.subjectTag.toLowerCase().includes(q))
                        || (f.branch && f.branch.toLowerCase().includes(q))
                        || (f.uploadedByName && f.uploadedByName.toLowerCase().includes(q))
                        || (f.yearOfStudy && String(f.yearOfStudy).includes(q))
                        || (f.semester && String(f.semester).includes(q))
                        || (f.fileType && f.fileType.toLowerCase().includes(q));
                    }).map(f => (
                      <tr key={f.fileId} className="animate-fade-in">
                        <td style={{ fontWeight: 600 }}>{f.originalName}</td>
                        <td><span className="badge badge-blue">{f.fileType}</span></td>
                        <td>{f.fileSizeDisplay}</td>
                        <td>{f.subjectTag || '—'}</td>
                        <td>{f.uploadedByName}</td>
                        <td style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                          {new Date(f.uploadedAt).toLocaleDateString()}
                        </td>
                        <td>
                          <div style={{ display: 'flex', gap: 6 }}>
                            {(['ADMIN', 'FACULTY'].includes(user?.role) || f.uploadedBy === user?.userId) && (
                              <button onClick={() => handleDelete(f.fileId)} className="btn btn-sm btn-danger">🗑 Delete</button>
                            )}
                            <button className="btn btn-sm btn-secondary" onClick={() => setPreviewFile(f)}>👁 Preview</button>
                            <a href={api.downloadUrl(f.fileId)} className="btn btn-sm btn-secondary"
                              target="_blank" rel="noopener noreferrer">⬇ Download</a>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Upload Modal */}
      <Modal open={showUpload} onClose={() => setShowUpload(false)} title="Upload File">
        <form onSubmit={handleUpload}>
          <div style={{ marginBottom: 14 }}>
            <label className="label">Files (select one or more)</label>
            <input type="file" className="input" onChange={e => setUploadFiles(Array.from(e.target.files))}
              multiple required />
            {uploadFiles.length > 1 && (
              <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 4 }}>
                {uploadFiles.length} files selected
              </div>
            )}
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 14 }}>
            <div>
              <label className="label">Branch *</label>
              <input className="input" value={branch} onChange={e => setBranch(e.target.value)}
                placeholder="e.g. CS, IT, ECE" required />
            </div>
            <div>
              <label className="label">Year</label>
              <select className="select" value={year} onChange={e => setYear(e.target.value)}>
                <option value="">Select</option>
                <option value="1">1st Year</option>
                <option value="2">2nd Year</option>
                <option value="3">3rd Year</option>
                <option value="4">4th Year</option>
              </select>
            </div>
            <div>
              <label className="label">Semester</label>
              <select className="select" value={semester} onChange={e => setSemester(e.target.value)}>
                <option value="">Select</option>
                {year 
                  ? [(year * 2) - 1, year * 2].map(s => <option key={s} value={s}>Semester {s}</option>)
                  : [1,2,3,4,5,6,7,8].map(s => <option key={s} value={s}>Semester {s}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Subject</label>
              <input className="input" value={subjectTag} onChange={e => setSubjectTag(e.target.value)}
                placeholder="e.g. DSA, OS" required />
            </div>
          </div>
          {uploading && (
            <div style={{ marginBottom: 14 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 6, color: 'var(--text-secondary)' }}>
                <span>Uploading file {uploadProgress.current} of {uploadProgress.total}...</span>
                <span>{Math.round((uploadProgress.current / uploadProgress.total) * 100)}%</span>
              </div>
              <div style={{ width: '100%', height: 8, background: 'var(--bg-secondary)', borderRadius: 4, overflow: 'hidden' }}>
                <div style={{
                  width: `${(uploadProgress.current / uploadProgress.total) * 100}%`,
                  height: '100%',
                  background: 'var(--gradient-1)',
                  borderRadius: 4,
                  transition: 'width 0.3s ease'
                }} />
              </div>
            </div>
          )}
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setShowUpload(false)} disabled={uploading}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={uploading}>
              {uploading ? `Uploading ${uploadProgress.current}/${uploadProgress.total}...` : 'Upload'}
            </button>
          </div>
        </form>
      </Modal>

      {previewFile && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.85)', zIndex: 9999,
          display: 'flex', flexDirection: 'column',
        }}>
          <div style={{
            display: 'flex', justifyContent: 'space-between', alignItems: 'center',
            padding: '12px 24px', background: 'var(--bg-card)', borderBottom: '1px solid var(--border)',
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <span style={{ fontSize: 16, fontWeight: 700, color: 'var(--text-primary)' }}>{previewFile.originalName}</span>
              <span className="badge badge-blue">{previewFile.fileType}</span>
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <a href={api.downloadUrl(previewFile.fileId)} className="btn btn-sm btn-secondary" target="_blank" rel="noopener noreferrer">⬇ Download</a>
              <button className="btn btn-sm btn-danger" onClick={() => setPreviewFile(null)}>✕ Close</button>
            </div>
          </div>
          <div style={{ flex: 1, padding: 0 }}>
            {(() => {
              const name = (previewFile.originalName || '').toLowerCase();
              const isOffice = name.endsWith('.docx') || name.endsWith('.pptx') || name.endsWith('.xlsx');
              const inlineUrl = `${api.downloadUrl(previewFile.fileId)}&inline=true`;
              if (isOffice) {
                return (
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', gap: 16 }}>
                    <div style={{ fontSize: 48 }}>📄</div>
                    <div style={{ fontSize: 15, color: 'var(--text-muted)' }}>Office documents cannot be previewed in the browser.</div>
                    <a href={api.downloadUrl(previewFile.fileId)} className="btn btn-primary" target="_blank" rel="noopener noreferrer">⬇ Download to View</a>
                  </div>
                );
              }
              return <iframe src={inlineUrl} style={{ width: '100%', height: '100%', border: 'none' }} title="Preview" />;
            })()}
          </div>
        </div>
      )}

      <ToastView />
    </>
  );
}
