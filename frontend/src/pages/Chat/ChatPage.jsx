import { useState, useEffect, useRef } from 'react';
import api from '../../api/client';
import { useAuth } from '../../contexts/AuthContext';
import { PageHeader, Spinner, EmptyState, Modal, useToast } from '../../components/UI';

export default function ChatPage() {
  const { user } = useAuth();
  const [tab, setTab] = useState('dm'); // dm | groups
  const [contacts, setContacts] = useState([]);
  const [groups, setGroups] = useState([]);
  const [selected, setSelected] = useState(null); // { type:'dm', username } or { type:'group', groupId, groupName }
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [showInvite, setShowInvite] = useState(false);
  const [groupName, setGroupName] = useState('');
  const [groupDesc, setGroupDesc] = useState('');
  const [inviteUser, setInviteUser] = useState('');
  const { showToast, ToastView } = useToast();
  const messagesEndRef = useRef(null);
  const pollRef = useRef(null);

  useEffect(() => {
    Promise.all([api.getChatContacts(), api.getMyChatGroups()])
      .then(([c, g]) => { setContacts(c); setGroups(g); })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!selected) return;
    loadMessages();
    pollRef.current = setInterval(loadMessages, 4000);
    return () => clearInterval(pollRef.current);
  }, [selected]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const loadMessages = async () => {
    try {
      if (selected.type === 'dm') {
        const data = await api.getChatHistory(selected.username);
        setMessages(data.messages || []);
      } else {
        const data = await api.getChatGroupMessages(selected.groupId);
        setMessages(data || []);
      }
    } catch { }
  };

  const handleSend = async (e) => {
    e.preventDefault();
    if (!input.trim() || !selected) return;
    try {
      if (selected.type === 'dm') {
        await api.sendChat(selected.username, input);
      } else {
        await api.sendChatGroupMessage(selected.groupId, input);
      }
      setInput('');
      loadMessages();
    } catch (err) {
      showToast(err.message, 'error');
    }
  };

  const handleCreateGroup = async (e) => {
    e.preventDefault();
    try {
      await api.createChatGroup({ name: groupName, description: groupDesc });
      showToast('Group created!');
      setShowCreate(false);
      setGroupName(''); setGroupDesc('');
      loadData();
    } catch (err) { showToast(err.message, 'error'); }
  };

  const handleDeleteGroup = async () => {
    if (!window.confirm('Are you sure you want to delete this group?')) return;
    try {
      await api.deleteChatGroup(selected.groupId);
      showToast('Group deleted');
      setSelected(null);
      loadData();
    } catch (err) { showToast(err.message, 'error'); }
  };

  const handleLeaveGroup = async () => {
    if (!window.confirm('Are you sure you want to leave this group?')) return;
    try {
      await api.leaveChatGroup(selected.groupId);
      showToast('You left the group');
      setSelected(null);
      loadData();
    } catch (err) { showToast(err.message, 'error'); }
  };

  const handleInvite = async (e) => {
    e.preventDefault();
    try {
      await api.inviteToChatGroup(selected.groupId, inviteUser);
      showToast(`Invited ${inviteUser}!`);
      setShowInvite(false);
      setInviteUser('');
    } catch (err) { showToast(err.message, 'error'); }
  };

  if (loading) return <Spinner />;

  return (
    <>
      <div className="chat-layout">
        {/* Sidebar */}
        <div className="chat-sidebar">
          <div style={{ display: 'flex', borderBottom: '1px solid var(--border)' }}>
            <button onClick={() => setTab('dm')} className="btn" style={{
              flex: 1, borderRadius: 0, background: tab === 'dm' ? 'var(--bg-card-hover)' : 'transparent',
              color: tab === 'dm' ? 'var(--accent-blue)' : 'var(--text-secondary)', fontSize: 13,
            }}>💬 Direct</button>
            <button onClick={() => setTab('groups')} className="btn" style={{
              flex: 1, borderRadius: 0, background: tab === 'groups' ? 'var(--bg-card-hover)' : 'transparent',
              color: tab === 'groups' ? 'var(--accent-blue)' : 'var(--text-secondary)', fontSize: 13,
            }}>👥 Groups</button>
          </div>

          {tab === 'groups' && (
            <div style={{ padding: '8px 12px', borderBottom: '1px solid var(--border)' }}>
              <button className="btn btn-primary btn-sm" style={{ width: '100%' }}
                onClick={() => setShowCreate(true)}>+ New Group</button>
            </div>
          )}

            <div style={{ padding: '8px 12px', borderBottom: '1px solid var(--border)' }}>
              <input type="text" className="input" placeholder="Search..." value={searchQuery} onChange={e => setSearchQuery(e.target.value)} />
            </div>

          <div style={{ overflowY: 'auto', flex: 1 }}>
            {tab === 'dm' ? (
              contacts.filter(c => !searchQuery || c.username.toLowerCase().includes(searchQuery.toLowerCase()) || (c.fullName && c.fullName.toLowerCase().includes(searchQuery.toLowerCase()))).length === 0 ? <EmptyState icon="👤" message="No contacts found" /> :
              contacts.filter(c => !searchQuery || c.username.toLowerCase().includes(searchQuery.toLowerCase()) || (c.fullName && c.fullName.toLowerCase().includes(searchQuery.toLowerCase()))).map(c => (
                <div key={c.username} className={`chat-contact ${selected?.type === 'dm' && selected.username === c.username ? 'active' : ''}`}
                  onClick={() => setSelected({ type: 'dm', username: c.username })}>
                  <div className="avatar avatar-sm">{c.fullName?.[0] || c.username[0]}</div>
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 600 }}>{c.fullName}</div>
                    <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>@{c.username} · {c.role}</div>
                  </div>
                </div>
              ))
            ) : (
              groups.filter(g => !searchQuery || g.groupName.toLowerCase().includes(searchQuery.toLowerCase())).length === 0 ? <EmptyState icon="👥" message="No groups found" /> :
              groups.filter(g => !searchQuery || g.groupName.toLowerCase().includes(searchQuery.toLowerCase())).map(g => (
                <div key={g.groupId} className={`chat-contact ${selected?.type === 'group' && selected.groupId === g.groupId ? 'active' : ''}`}
                  onClick={() => setSelected({ type: 'group', groupId: g.groupId, groupName: g.groupName })}>
                  <div className="avatar avatar-sm" style={{ background: 'var(--gradient-2)' }}>
                    {g.groupName[0]}
                  </div>
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 600 }}>{g.groupName}</div>
                    <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{g.myRole}</div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Chat main */}
        <div className="chat-main">
          {!selected ? (
            <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <EmptyState icon="💬" message="Select a contact or group to start chatting" />
            </div>
          ) : (
            <>
              {/* Chat header */}
              <div style={{
                padding: '14px 20px', borderBottom: '1px solid var(--border)',
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                background: 'var(--bg-secondary)',
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <div className="avatar avatar-sm" style={{ background: selected.type === 'group' ? 'var(--gradient-2)' : undefined }}>
                    {selected.type === 'dm' ? selected.username[0].toUpperCase() : '👥'}
                  </div>
                  <div>
                    <h3 style={{ fontSize: 16, fontWeight: 700 }}>{selected.type === 'dm' ? selected.username : selected.groupName}</h3>
                    {selected.type === 'group' && <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>Group Chat</div>}
                  </div>
                </div>
                {selected.type === 'group' && (
                  <div style={{ display: 'flex', gap: 8 }}>
                    <button className="btn btn-sm btn-secondary" onClick={() => setShowInvite(true)}>+ Invite</button>
                    {groups.find(g => g.groupId === selected.groupId)?.myRole === 'OWNER' ? (
                      <button className="btn btn-sm btn-danger" onClick={handleDeleteGroup}>🗑 Delete</button>
                    ) : (
                      <button className="btn btn-sm btn-danger" onClick={handleLeaveGroup}>👋 Leave</button>
                    )}
                  </div>
                )}
              </div>

              {/* Messages */}
              <div className="chat-messages">
                {messages.length === 0 ? (
                  <EmptyState icon="💭" message="No messages yet. Say hello!" />
                ) : messages.map((m, i) => (
                  <div key={m.messageId || i} style={{ display: 'flex', flexDirection: 'column', alignItems: m.fromSelf ? 'flex-end' : 'flex-start' }}>
                    {selected.type === 'group' && !m.fromSelf && (
                      <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 2, marginLeft: 4 }}>
                        {m.senderName || m.senderUsername}
                      </div>
                    )}
                    <div className={`chat-bubble ${m.fromSelf ? 'sent' : 'received'}`}>
                      {m.content}
                    </div>
                  </div>
                ))}
                <div ref={messagesEndRef} />
              </div>

              {/* Input */}
              <form className="chat-input-area" onSubmit={handleSend}>
                <input className="input" value={input} onChange={e => setInput(e.target.value)}
                  placeholder="Type a message..." style={{ flex: 1 }} />
                <button type="submit" className="btn btn-primary">Send</button>
              </form>
            </>
          )}
        </div>
      </div>

      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="Create Chat Group">
        <form onSubmit={handleCreateGroup}>
          <div style={{ marginBottom: 14 }}>
            <label className="label">Group Name</label>
            <input className="input" value={groupName} onChange={e => setGroupName(e.target.value)} required />
          </div>
          <div style={{ marginBottom: 14 }}>
            <label className="label">Description (optional)</label>
            <textarea className="textarea" value={groupDesc} onChange={e => setGroupDesc(e.target.value)} />
          </div>
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setShowCreate(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Create</button>
          </div>
        </form>
      </Modal>

      <Modal open={showInvite} onClose={() => setShowInvite(false)} title="Invite to Group">
        <form onSubmit={handleInvite}>
          <div style={{ marginBottom: 14 }}>
            <label className="label">Username to invite</label>
            <input className="input" value={inviteUser} onChange={e => setInviteUser(e.target.value)} required placeholder="Enter username" />
          </div>
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setShowInvite(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Invite</button>
          </div>
        </form>
      </Modal>
      <ToastView />
    </>
  );
}
