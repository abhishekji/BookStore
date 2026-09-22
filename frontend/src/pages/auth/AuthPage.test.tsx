import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthPage } from './AuthPage';
import { AuthProvider } from '../../application/auth-state/AuthState';
import { authApi, TOKEN_KEY } from '../../api/client/client';
import { Provider } from 'react-redux';
import { hideToast, store } from '../../state/store';
import { Toast } from '../../components/toast/Toast';

vi.mock('../../api/client/client', async () => {
  const actual = await vi.importActual<typeof import('../../api/client/client')>('../../api/client/client');
  return { ...actual, authApi: { login: vi.fn(), register: vi.fn() } };
});

const credentials = { token: 'token', email: 'reader@example.com', displayName: 'Reader' };
const renderPage = (props: Parameters<typeof AuthPage>[0] = {}) =>
  render(<Provider store={store}><Toast /><AuthProvider><AuthPage {...props} /></AuthProvider></Provider>);

describe('AuthPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    store.dispatch(hideToast());
  });
  afterEach(() => localStorage.clear());

  it('returns to login mode after successful registration', async () => {
    vi.mocked(authApi.register).mockResolvedValue(credentials);
    const user = userEvent.setup();
    renderPage();
    await user.click(screen.getByRole('button', { name: 'Create an account' }));
    await user.type(screen.getByLabelText('Name'), 'Reader');
    await user.type(screen.getByLabelText('Email'), 'reader@example.com');
    await user.type(screen.getByLabelText('Password'), 'StrongPass1');
    await user.click(screen.getByRole('button', { name: 'Create account' }));
    expect(await screen.findByRole('status')).toHaveTextContent('Registration successful');
    expect(screen.getByRole('heading', { name: 'Welcome back' })).toBeInTheDocument();
  });

  it('signs the reader in with a normalized email address', async () => {
    vi.mocked(authApi.login).mockResolvedValue(credentials);
    const onLoginSuccess = vi.fn();
    const user = userEvent.setup();

    renderPage({ onLoginSuccess });
    await user.type(screen.getByLabelText('Email'), '  Reader@Example.com  ');
    await user.type(screen.getByLabelText('Password'), 'StrongPass1');
    await user.click(screen.getByRole('button', { name: 'Log in' }));

    expect(authApi.login).toHaveBeenCalledWith('reader@example.com', 'StrongPass1');
    expect(onLoginSuccess).toHaveBeenCalledTimes(1);
  });

  it('reports rejected credentials without signing the reader in', async () => {
    vi.mocked(authApi.login).mockRejectedValue(new Error('Invalid email or password'));
    const user = userEvent.setup();

    renderPage();
    await user.type(screen.getByLabelText('Email'), 'reader@example.com');
    await user.type(screen.getByLabelText('Password'), 'WrongPass1');
    await user.click(screen.getByRole('button', { name: 'Log in' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid email or password');
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });

  it('refuses a short password before calling the API', async () => {
    const user = userEvent.setup();

    renderPage();
    await user.type(screen.getByLabelText('Email'), 'reader@example.com');
    await user.type(screen.getByLabelText('Password'), 'short');
    await user.click(screen.getByRole('button', { name: 'Log in' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Password must contain at least 8 characters.');
    expect(authApi.login).not.toHaveBeenCalled();
  });

  it('refuses a registration without a usable display name', async () => {
    const user = userEvent.setup();

    renderPage({ mode: 'register' });
    await user.type(screen.getByLabelText('Name'), 'R');
    await user.type(screen.getByLabelText('Email'), 'reader@example.com');
    await user.type(screen.getByLabelText('Password'), 'StrongPass1');
    await user.click(screen.getByRole('button', { name: 'Create account' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Name must contain at least 2 characters.');
    expect(authApi.register).not.toHaveBeenCalled();
  });

  it('follows the mode chosen by the surrounding router', () => {
    const { rerender } = renderPage({ mode: 'login' });
    expect(screen.getByRole('heading', { name: 'Welcome back' })).toBeInTheDocument();

    rerender(<Provider store={store}><Toast /><AuthProvider><AuthPage mode="register" /></AuthProvider></Provider>);

    expect(screen.getByRole('heading', { name: 'Create your account' })).toBeInTheDocument();
  });

  it('reports the mode change when the reader switches forms', async () => {
    const onModeChange = vi.fn();
    const user = userEvent.setup();

    renderPage({ onModeChange });
    await user.click(screen.getByRole('button', { name: 'Create an account' }));
    expect(onModeChange).toHaveBeenLastCalledWith('register');

    await user.click(screen.getByRole('button', { name: 'Use existing account' }));
    expect(onModeChange).toHaveBeenLastCalledWith('login');
  });

  it('offers a sign-out to a reader who is already authenticated', async () => {
    localStorage.setItem(TOKEN_KEY, 'token');
    const user = userEvent.setup();

    renderPage();
    await user.click(screen.getByRole('button', { name: 'Log out' }));

    expect(screen.getByRole('heading', { name: 'Welcome back' })).toBeInTheDocument();
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });
});
