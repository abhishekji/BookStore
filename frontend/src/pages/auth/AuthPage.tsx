import { useEffect, useState, type FormEvent } from 'react';
import { useAuth } from '../../application/auth-state/AuthState';
import './AuthPage.css';
import { useAppDispatch } from '../../state/hooks';
import { showToast } from '../../state/store';

export function AuthPage({ mode = 'login', onLoginSuccess, onModeChange }: {
  mode?: 'login' | 'register';
  onLoginSuccess?: () => void;
  onModeChange?: (mode: 'login' | 'register') => void;
}) {
  const { isAuthenticated, login, register, logout } = useAuth();
  const dispatch = useAppDispatch();
  const [registerMode, setRegisterMode] = useState(mode === 'register');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  useEffect(() => {
    setRegisterMode(mode === 'register');
  }, [mode]);
  if (isAuthenticated) return <button type="button" onClick={logout}>Log out</button>;
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const normalizedEmail = email.trim().toLowerCase();
    const normalizedName = displayName.trim();
    if (registerMode && normalizedName.length < 2) {
      dispatch(showToast({ type: 'error', message: 'Name must contain at least 2 characters.' }));
      return;
    }
    if (password.length < 8) {
      dispatch(showToast({ type: 'error', message: 'Password must contain at least 8 characters.' }));
      return;
    }
    setIsSubmitting(true);
    try {
      if (registerMode) {
        await register(normalizedEmail, password, normalizedName);
        setRegisterMode(false);
        onModeChange?.('login');
        setPassword('');
        dispatch(showToast({ type: 'success', message: 'Registration successful. Please log in to continue.' }));
      } else {
        await login(normalizedEmail, password);
        onLoginSuccess?.();
      }
    } catch (submissionError) {
      dispatch(showToast({
        type: 'error',
        message: submissionError instanceof Error ? submissionError.message : 'Unable to authenticate with those credentials.',
      }));
    } finally {
      setIsSubmitting(false);
    }
  };
  return <main className="auth-page"><section className="auth-card">
    <div className="auth-brand">BOOK<span>HUB</span></div>
    <p className="auth-eyebrow">Your next great read awaits</p>
    <h1>{registerMode ? 'Create your account' : 'Welcome back'}</h1>
    <p className="auth-subtitle">{registerMode ? 'Join our community of readers.' : 'Sign in to continue shopping.'}</p>
    <form onSubmit={submit}>
    {registerMode && <input aria-label="Name" name="name" placeholder="Enter display name" autoComplete="name" required value={displayName} onChange={event => setDisplayName(event.target.value)} />}
    <input aria-label="Email" name="email" placeholder="Enter email address" autoComplete="email" type="email" required value={email} onChange={event => setEmail(event.target.value)} />
    <input aria-label="Password" name="password" placeholder="Enter password" autoComplete={registerMode ? 'new-password' : 'current-password'} minLength={8} type="password" required value={password} onChange={event => setPassword(event.target.value)} />
    <button className="primary-button" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Please wait...' : registerMode ? 'Create account' : 'Log in'}</button>
    </form>
    <button className="link-button" type="button" onClick={() => {
      const nextMode = registerMode ? 'login' : 'register';
      setRegisterMode(!registerMode);
      onModeChange?.(nextMode);
    }}>
      {registerMode ? 'Use existing account' : 'Create an account'}
    </button>
  </section></main>;
}
