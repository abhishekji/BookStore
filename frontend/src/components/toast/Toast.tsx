import { useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../../state/hooks';
import { hideToast } from '../../state/store';
import './Toast.css';

export function Toast() {
  const dispatch = useAppDispatch();
  const toast = useAppSelector(state => state.toast);

  useEffect(() => {
    if (!toast.message) return;
    const timeout = window.setTimeout(() => dispatch(hideToast()), 4000);
    return () => window.clearTimeout(timeout);
  }, [dispatch, toast.message]);

  if (!toast.message) return null;
  return (
    <div className={`toast toast--${toast.type}`} role={toast.type === 'error' ? 'alert' : 'status'}>
      {toast.message}
      <button type="button" aria-label="Dismiss notification" onClick={() => dispatch(hideToast())}>×</button>
    </div>
  );
}
