const API_BASE = '/api';

async function request(url, options = {}) {
  const config = {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  };

  if (config.body && typeof config.body === 'object' && !(config.body instanceof FormData)) {
    config.body = JSON.stringify(config.body);
  }

  if (config.body instanceof FormData) {
    delete config.headers['Content-Type'];
  }

  const res = await fetch(`${API_BASE}${url}`, config);

  if (res.status === 401) {
    const event = new CustomEvent('auth:unauthorized');
    window.dispatchEvent(event);
    throw new Error('Unauthorized');
  }

  if (!res.ok) {
    const data = await res.json().catch(() => ({}));
    throw new Error(data.error || `Request failed (${res.status})`);
  }

  const contentType = res.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return res.json();
  }
  return res;
}

const api = {
  // Auth
  login: (username, password) => request('/login', { method: 'POST', body: { username, password } }),
  logout: () => request('/logout', { method: 'POST' }),
  me: () => request('/auth/me'),

  // Dashboard
  dashboard: () => request('/dashboard'),

  // Notices
  getNotices: () => request('/notices'),
  postNotice: (title, body) => request('/notices', {
    method: 'POST',
    body: JSON.stringify({ title, body })
  }),
  updateNotice: (id, title, body) => request(`/notices/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ title, body })
  }),
  deleteNotice: (id) => request(`/notices/${id}`, {
    method: 'DELETE'
  }),

  // Notice Groups
  getMyNoticeGroups: () => request('/notice-groups/'),
  getDiscoverableGroups: () => request('/notice-groups/discover'),
  getNoticeGroupInfo: (id) => request(`/notice-groups/${id}`),
  createNoticeGroup: (data) => request('/notice-groups/', { method: 'POST', body: data }),
  getGroupNotices: (id) => request(`/notice-groups/${id}/notices`),
  postGroupNotice: (id, title, body) => request(`/notice-groups/${id}/notices`, { method: 'POST', body: { title, body } }),
  inviteToNoticeGroup: (id, username) => request(`/notice-groups/${id}/invite`, { method: 'POST', body: { username } }),
  requestJoinNoticeGroup: (id) => request(`/notice-groups/${id}/request`, { method: 'POST' }),
  getNoticeGroupMembers: (id) => request(`/notice-groups/${id}/members`),
  getPendingRequests: (id) => request(`/notice-groups/${id}/pending`),
  approveMember: (groupId, userId) => request(`/notice-groups/${groupId}/approve-member`, { method: 'POST', body: { userId } }),
  deleteNoticeGroup: (id) => request(`/notice-groups/${id}`, { method: 'DELETE' }),
  leaveNoticeGroup: (id) => request(`/notice-groups/${id}/leave`, { method: 'DELETE' }),

  // Files
  getFiles: (params) => {
    const qs = params ? '?' + new URLSearchParams(params).toString() : '';
    return request(`/files/${qs}`);
  },
  getPendingFiles: () => request('/files/pending'),
  approveFile: (id) => request(`/files/${id}/approve`, { method: 'POST' }),
  rejectFile: (id) => request(`/files/${id}/reject`, { method: 'POST' }),
  uploadFile: (formData) => request('/files/', {
    method: 'POST',
    body: formData,
    headers: {},
  }),
  deleteFile: (fileId) => request(`/files/${fileId}`, { method: 'DELETE' }),
  downloadUrl: (fileId) => `${API_BASE}/files/download?fileId=${fileId}`,

  // Chat (DM)
  getChatContacts: () => request('/chat/contacts'),
  getChatHistory: (username) => request(`/chat/history?with=${encodeURIComponent(username)}`),
  sendChat: (to, message) => request('/chat/send', { method: 'POST', body: { to, message } }),

  // Chat Groups
  getMyChatGroups: () => request('/chat-groups/'),
  createChatGroup: (data) => request('/chat-groups/', { method: 'POST', body: data }),
  getChatGroupMessages: (id) => request(`/chat-groups/${id}/messages`),
  sendChatGroupMessage: (id, message) => request(`/chat-groups/${id}/send`, { method: 'POST', body: { message } }),
  getChatGroupMembers: (id) => request(`/chat-groups/${id}/members`),
  inviteToChatGroup: (id, username) => request(`/chat-groups/${id}/invite`, { method: 'POST', body: { username } }),
  deleteChatGroup: (id) => request(`/chat-groups/${id}`, { method: 'DELETE' }),
  leaveChatGroup: (id) => request(`/chat-groups/${id}/leave`, { method: 'DELETE' }),

  // Status
  getOnlineUsers: () => request('/status'),
  checkAlert: () => request('/status?type=alert'),
  checkNotice: () => request('/status?type=notice'),

  // Events
  getEvents: (month) => request(`/events${month ? '?month=' + month : ''}`),

  // Admin
  sendAlert: (message) => request('/alerts', { method: 'POST', body: { message } }),
  getLogs: () => request('/logs'),

  // Users
  searchUsers: (q) => request(`/users${q ? '?q=' + encodeURIComponent(q) : ''}`),
};

export default api;
