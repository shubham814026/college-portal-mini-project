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
  const { showToast, ToastView } = useToast();

  // Upload form state
  const [uploadFile, setUploadFile] = useState(null);
  const [branch, setBranch] = useState('');
  const [year, setYear] = useState('');
  const [semester, setSemester] = useState('');
  const [subjectTag, setSubjectTag] = useState('');

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
    if (!uploadFile) return;
    const formData = new FormData();
    formData.append('file', uploadFile);
    if (branch) formData.append('branch', branch);
    if (year) formData.append('year', year);
    if (semester) formData.append('semester', semester);
    if (subjectTag) formData.append('subjectTag', subjectTag);

    try {
      const res = await api.uploadFile(formData);
      showToast(res.approvalStatus === 'PENDING'
        ? 'File uploaded! Awaiting approval.'
        : 'File uploaded successfully!');
      setShowUpload(false);
      setUploadFile(null);
      setBranch(''); setYear(''); setSemester(''); setSubjectTag('');
      loadFiles();
    } catch (err) {
      showToast(err.message, 'error');
    }
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

            {files.length === 0 ? (
              <EmptyState icon="📂" message="No files in this folder." />
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
                    {files.map(f => (
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
                          {['ADMIN', 'FACULTY'].includes(user?.role) && (
                            <button onClick={() => handleDelete(f.fileId)} className="btn btn-sm btn-danger" style={{marginRight: 8}}>🗑 Delete</button>
                          )}
                          <a href={api.downloadUrl(f.fileId)} className="btn btn-sm btn-secondary"
                            target="_blank" rel="noopener noreferrer">⬇ Download</a>
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
            <label className="label">File (pdf, docx, pptx, zip — max 10MB)</label>
            <input type="file" className="input" onChange={e => setUploadFile(e.target.files[0])}
              accept=".pdf,.docx,.pptx,.zip" required />
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 14 }}>
            <div>
              <label className="label">Branch</label>
              <input className="input" value={branch} onChange={e => setBranch(e.target.value)}
                placeholder="e.g. CS, IT, ECE" />
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
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setShowUpload(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Upload</button>
          </div>
        </form>
      </Modal>

      <ToastView />
    </>
  );
}
