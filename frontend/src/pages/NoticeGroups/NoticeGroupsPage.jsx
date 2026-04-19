import { useState, useEffect } from 'react';
import api from '../../api/client';
import { useAuth } from '../../contexts/AuthContext';
import { PageHeader, Spinner, EmptyState, Modal, useToast } from '../../components/UI';

export default function NoticeGroupsPage() {
  const { user } = useAuth();
  const [myGroups, setMyGroups] = useState([]);
  const [discoverGroups, setDiscoverGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedGroup, setSelectedGroup] = useState(null);
  const [notices, setNotices] = useState([]);
  const [members, setMembers] = useState([]);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [showCreate, setShowCreate] = useState(false);
  const [showInvite, setShowInvite] = useState(false);
  const [showPostNotice, setShowPostNotice] = useState(false);
  const [groupName, setGroupName] = useState('');
  const [groupDesc, setGroupDesc] = useState('');
  const [joinPolicy, setJoinPolicy] = useState('INVITE_ONLY');
  const [inviteUser, setInviteUser] = useState('');
  const [noticeTitle, setNoticeTitle] = useState('');
  const [noticeBody, setNoticeBody] = useState('');
  const [tab, setTab] = useState('my'); // my | discover
  const { showToast, ToastView } = useToast();

  const loadData = async () => {
    try {
      const [my, discover] = await Promise.all([
        api.getMyNoticeGroups(),
        api.getDiscoverableGroups(),
      ]);
      setMyGroups(my);
      setDiscoverGroups(discover);
    } catch (e) { console.error(e); }
    setLoading(false);
  };

  useEffect(() => { loadData(); }, []);

  const selectGroup = async (g) => {
    setSelectedGroup(g);
    try {
      const [n, m] = await Promise.all([
        api.getGroupNotices(g.groupId),
        api.getNoticeGroupMembers(g.groupId),
      ]);
      setNotices(n);
      setMembers(m);
      if (g.myRole === 'OWNER' || g.myRole === 'ADMIN') {
        const p = await api.getPendingRequests(g.groupId);
        setPendingRequests(p);
      } else {
        setPendingRequests([]);
      }
    } catch (e) { console.error(e); }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await api.createNoticeGroup({ name: groupName, description: groupDesc, joinPolicy });
      showToast('Group created!');
      setShowCreate(false);
      setGroupName(''); setGroupDesc(''); setJoinPolicy('INVITE_ONLY');
      loadData();
    } catch (err) { showToast(err.message, 'error'); }
  };

  const handleInvite = async (e) => {
    e.preventDefault();
    try {
      await api.inviteToNoticeGroup(selectedGroup.groupId, inviteUser);
      showToast(`Invited ${inviteUser}!`);
      setShowInvite(false);
      setInviteUser('');
      selectGroup(selectedGroup);
    } catch (err) { showToast(err.message, 'error'); }
  };

  const handlePostNotice = async (e) => {
    e.preventDefault();
    try {
      await api.postGroupNotice(selectedGroup.groupId, noticeTitle, noticeBody);
      showToast('Notice posted!');
      setShowPostNotice(false);
      setNoticeTitle(''); setNoticeBody('');
      selectGroup(selectedGroup);
    } catch (err) { showToast(err.message, 'error'); }
  };

  const handleRequestJoin = async (groupId) => {
    try {
      await api.requestJoinNoticeGroup(groupId);
      showToast('Join request sent!');
      loadData();
    } catch (err) { showToast(err.message, 'error'); }
  };

  const handleApprove = async (userId) => {
    try {
      await api.approveMember(selectedGroup.groupId, userId);
      showToast('Member approved!');
      selectGroup(selectedGroup);
    } catch (err) { showToast(err.message, 'error'); }
  };

  if (loading) return <Spinner />;

  return (
    <>
      <PageHeader title="📋 Notice Groups" subtitle="Private notice boards with invite or request-join access"
        actions={<button className="btn btn-primary" onClick={() => setShowCreate(true)}>+ Create Group</button>} />
      <div className="page-body">
        <div style={{ display: 'flex', gap: 24 }}>
          {/* Group list */}
          <div style={{ width: 300, flexShrink: 0 }}>
            <div style={{ display: 'flex', gap: 4, marginBottom: 12 }}>
              <button className={`btn btn-sm ${tab === 'my' ? 'btn-primary' : 'btn-secondary'}`}
                onClick={() => setTab('my')}>My Groups</button>
              <button className={`btn btn-sm ${tab === 'discover' ? 'btn-primary' : 'btn-secondary'}`}
                onClick={() => setTab('discover')}>Discover</button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {(tab === 'my' ? myGroups : discoverGroups).length === 0 ? (
                <EmptyState icon="📋" message={tab === 'my' ? "No groups joined yet" : "No groups to discover"} />
              ) : (tab === 'my' ? myGroups : discoverGroups).map(g => (
                <div key={g.groupId} className="glass-card" style={{
                  padding: 14, cursor: 'pointer',
                  borderColor: selectedGroup?.groupId === g.groupId ? 'var(--accent-blue)' : undefined,
                }} onClick={() => tab === 'my' ? selectGroup(g) : null}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ fontWeight: 700, fontSize: 14 }}>{g.groupName}</div>
                    <span className={`badge ${g.joinPolicy === 'REQUEST_JOIN' ? 'badge-amber' : 'badge-purple'}`}>
                      {g.joinPolicy === 'REQUEST_JOIN' ? 'Open' : 'Invite'}
                    </span>
                  </div>
                  {g.description && <p style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 4 }}>{g.description}</p>}
                  <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 6 }}>
                    by {g.creatorName} {g.myRole && <span>• {g.myRole}</span>}
                  </div>
                  {tab === 'discover' && (
                    <button className="btn btn-sm btn-primary" style={{ marginTop: 8, width: '100%' }}
                      onClick={(e) => { e.stopPropagation(); handleRequestJoin(g.groupId); }}>
                      Request to Join
                    </button>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Group detail */}
          <div style={{ flex: 1 }}>
            {!selectedGroup ? (
              <EmptyState icon="📋" message="Select a group to view its notices" />
            ) : (
              <>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
                  <div>
                    <h2 style={{ fontSize: 20, fontWeight: 700 }}>{selectedGroup.groupName}</h2>
                    <p style={{ fontSize: 13, color: 'var(--text-muted)' }}>{members.length} members</p>
                  </div>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <button className="btn btn-sm btn-secondary" onClick={() => setShowInvite(true)}>+ Invite</button>
                    <button className="btn btn-sm btn-primary" onClick={() => setShowPostNotice(true)}>📝 Post Notice</button>
                  </div>
                </div>

                {/* Pending Requests */}
                {pendingRequests.length > 0 && (
                  <div className="glass-card" style={{ padding: 16, marginBottom: 16, borderColor: 'var(--accent-amber)' }}>
                    <h4 style={{ fontSize: 14, fontWeight: 700, marginBottom: 10, color: 'var(--accent-amber)' }}>
                      ⏳ Pending Join Requests ({pendingRequests.length})
                    </h4>
                    {pendingRequests.map(r => (
                      <div key={r.userId} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 0' }}>
                        <span>{r.fullName} (@{r.username})</span>
                        <button className="btn btn-sm btn-success" onClick={() => handleApprove(r.userId)}>✅ Approve</button>
                      </div>
                    ))}
                  </div>
                )}

                {/* Notices */}
                {notices.length === 0 ? (
                  <EmptyState icon="📢" message="No notices in this group yet" />
                ) : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                    {notices.map(n => (
                      <div key={n.id} className="glass-card animate-fade-in" style={{ padding: 20 }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                          <h3 style={{ fontSize: 16, fontWeight: 700 }}>{n.title}</h3>
                          <span className="badge badge-blue">{new Date(n.createdAt).toLocaleDateString()}</span>
                        </div>
                        <p style={{ color: 'var(--text-secondary)', marginTop: 8, lineHeight: 1.7, fontSize: 14 }}>{n.body}</p>
                        <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 8 }}>by {n.postedByName}</div>
                      </div>
                    ))}
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>

      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="Create Notice Group">
        <form onSubmit={handleCreate}>
          <div style={{ marginBottom: 14 }}>
            <label className="label">Group Name</label>
            <input className="input" value={groupName} onChange={e => setGroupName(e.target.value)} required />
          </div>
          <div style={{ marginBottom: 14 }}>
            <label className="label">Description</label>
            <textarea className="textarea" value={groupDesc} onChange={e => setGroupDesc(e.target.value)} />
          </div>
          <div style={{ marginBottom: 14 }}>
            <label className="label">Join Policy</label>
            <select className="select" value={joinPolicy} onChange={e => setJoinPolicy(e.target.value)}>
              <option value="INVITE_ONLY">Invite Only</option>
              <option value="REQUEST_JOIN">Request to Join (discoverable)</option>
            </select>
          </div>
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setShowCreate(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Create</button>
          </div>
        </form>
      </Modal>

      <Modal open={showInvite} onClose={() => setShowInvite(false)} title="Invite Member">
        <form onSubmit={handleInvite}>
          <div style={{ marginBottom: 14 }}>
            <label className="label">Username</label>
            <input className="input" value={inviteUser} onChange={e => setInviteUser(e.target.value)} required placeholder="Enter username" />
          </div>
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setShowInvite(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Invite</button>
          </div>
        </form>
      </Modal>

      <Modal open={showPostNotice} onClose={() => setShowPostNotice(false)} title="Post Group Notice">
        <form onSubmit={handlePostNotice}>
          <div style={{ marginBottom: 14 }}>
            <label className="label">Title</label>
            <input className="input" value={noticeTitle} onChange={e => setNoticeTitle(e.target.value)} required />
          </div>
          <div style={{ marginBottom: 14 }}>
            <label className="label">Body</label>
            <textarea className="textarea" value={noticeBody} onChange={e => setNoticeBody(e.target.value)} required rows={4} />
          </div>
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setShowPostNotice(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Post</button>
          </div>
        </form>
      </Modal>
      <ToastView />
    </>
  );
}
