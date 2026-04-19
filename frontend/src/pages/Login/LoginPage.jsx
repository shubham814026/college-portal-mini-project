import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(username, password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
      background: 'linear-gradient(135deg, #0f172a 0%, #1e1b4b 50%, #0f172a 100%)',
      padding: 20,
    }}>
      <div className="glass-card animate-fade-in" style={{ width: '100%', maxWidth: 420, padding: 40 }}>
        {/* Logo */}
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div style={{
            width: 64, height: 64, margin: '0 auto 16px', borderRadius: 16,
            background: 'var(--gradient-1)', display: 'flex',
            alignItems: 'center', justifyContent: 'center', fontSize: 32,
          }}>🎓</div>
          <h1 style={{ fontSize: 24, fontWeight: 800, letterSpacing: '-1px' }}>College Portal</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: 14, marginTop: 4 }}>
            Sign in to your account
          </p>
        </div>

        {error && (
          <div style={{
            background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)',
            borderRadius: 8, padding: '10px 14px', marginBottom: 20, fontSize: 13,
            color: '#fca5a5', textAlign: 'center',
          }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: 16 }}>
            <label className="label">Username</label>
            <input className="input" type="text" value={username}
              onChange={e => setUsername(e.target.value)} placeholder="Enter username"
              required autoFocus autoComplete="username" />
          </div>
          <div style={{ marginBottom: 24 }}>
            <label className="label">Password</label>
            <input className="input" type="password" value={password}
              onChange={e => setPassword(e.target.value)} placeholder="Enter password"
              required autoComplete="current-password" />
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading}
            style={{ width: '100%', padding: '12px 20px', fontSize: 15 }}>
            {loading ? '⏳ Signing in...' : '→ Sign In'}
          </button>
        </form>

        <div style={{ marginTop: 24, padding: 16, borderRadius: 8, background: 'rgba(59,130,246,0.08)', border: '1px solid rgba(59,130,246,0.15)' }}>
          <p style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 8, fontWeight: 600 }}>Demo Credentials</p>
          <div style={{ display: 'grid', gap: 4, fontSize: 12, color: 'var(--text-secondary)' }}>
            <span>👨‍💼 Admin: <code>admin</code> / <code>admin123</code></span>
            <span>👨‍🏫 Faculty: <code>faculty</code> / <code>faculty123</code></span>
            <span>👨‍🎓 Student: <code>rahul</code> / <code>rahul123</code></span>
          </div>
        </div>
      </div>
    </div>
  );
}
