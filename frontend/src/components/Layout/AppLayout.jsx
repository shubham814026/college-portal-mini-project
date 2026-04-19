import { useEffect, useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import api from '../../api/client';
import { useToast } from '../UI';

export default function AppLayout() {
  const { showToast, ToastView } = useToast();
  const [globalAlert, setGlobalAlert] = useState(null);

  useEffect(() => {
    const poll = async () => {
      try {
        const { alert } = await api.checkAlert();
        if (alert) {
          setGlobalAlert(alert);
          showToast(`🚨 ALERT: ${alert}`, 'error');
          // Hide banner after 10s
          setTimeout(() => setGlobalAlert(null), 10000);
        }
      } catch (e) { }
      
      try {
        const { notice } = await api.checkNotice();
        if (notice) {
          showToast('📢 New notice posted!', 'success');
        }
      } catch (e) { }
    };

    const intervalId = setInterval(poll, 5000);
    poll(); // Initial check
    return () => clearInterval(intervalId);
  }, []);

  return (
    <div className="app-layout">
      <Sidebar />
      <main className="main-content">
        {globalAlert && (
          <div style={{ background: 'var(--accent-red)', color: 'white', padding: '12px 24px', fontWeight: 600, textAlign: 'center', zIndex: 100 }}>
            🚨 {globalAlert}
          </div>
        )}
        <Outlet />
        <ToastView />
      </main>
    </div>
  );
}
