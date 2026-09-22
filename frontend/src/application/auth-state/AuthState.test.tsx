import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider, useAuth } from './AuthState';
import { authApi, TOKEN_KEY } from '../../api/client/client';

vi.mock('../../api/client/client', async () => {
  const actual = await vi.importActual<typeof import('../../api/client/client')>('../../api/client/client');
  return { ...actual, authApi: { login: vi.fn(), register: vi.fn() } };
});

const credentials = { token: 'jwt-token', email: 'reader@example.com', displayName: 'Reader' };

function AuthConsumer() {
  const { isAuthenticated, setAuthenticated } = useAuth();
  return <button onClick={() => setAuthenticated(true)}>
    {isAuthenticated ? 'authenticated' : 'anonymous'}
  </button>;
}

/** Exercises the credential-carrying part of the context that AuthConsumer does not touch. */
function AuthActions() {
  const { isAuthenticated, user, login, register, logout } = useAuth();
  return <>
    <p data-testid="identity">{user?.displayName ?? 'nobody'} / {isAuthenticated ? 'in' : 'out'}</p>
    <button type="button" onClick={() => void login('reader@example.com', 'StrongPass1')}>Log in</button>
    <button type="button" onClick={() => void register('reader@example.com', 'StrongPass1', 'Reader')}>Register</button>
    <button type="button" onClick={logout}>Log out</button>
  </>;
}

describe('AuthState', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });
  afterEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('starts anonymous and updates authentication state', async () => {
    const user = userEvent.setup();
    render(<AuthProvider><AuthConsumer /></AuthProvider>);
    const button = screen.getByRole('button', { name: 'anonymous' });
    await user.click(button);
    expect(screen.getByRole('button', { name: 'authenticated' })).toBeInTheDocument();
  });

  it('rejects use outside its provider', () => {
    vi.spyOn(console, 'error').mockImplementation(() => {});
    expect(() => render(<AuthConsumer />)).toThrow('useAuth must be used within AuthProvider');
  });

  it('restores an authenticated session from a stored token', () => {
    localStorage.setItem(TOKEN_KEY, 'jwt-token');
    render(<AuthProvider><AuthConsumer /></AuthProvider>);
    expect(screen.getByRole('button', { name: 'authenticated' })).toBeInTheDocument();
  });

  it('stores the token and the signed-in reader on login', async () => {
    vi.mocked(authApi.login).mockResolvedValue(credentials);
    const user = userEvent.setup();

    render(<AuthProvider><AuthActions /></AuthProvider>);
    await user.click(screen.getByRole('button', { name: 'Log in' }));

    expect(authApi.login).toHaveBeenCalledWith('reader@example.com', 'StrongPass1');
    expect(await screen.findByTestId('identity')).toHaveTextContent('Reader / in');
    expect(localStorage.getItem(TOKEN_KEY)).toBe('jwt-token');
  });

  it('delegates registration without signing the reader in', async () => {
    vi.mocked(authApi.register).mockResolvedValue(credentials);
    const user = userEvent.setup();

    render(<AuthProvider><AuthActions /></AuthProvider>);
    await user.click(screen.getByRole('button', { name: 'Register' }));

    expect(authApi.register).toHaveBeenCalledWith('reader@example.com', 'StrongPass1', 'Reader');
    expect(screen.getByTestId('identity')).toHaveTextContent('nobody / out');
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });

  it('discards the token and the reader on logout', async () => {
    vi.mocked(authApi.login).mockResolvedValue(credentials);
    const user = userEvent.setup();

    render(<AuthProvider><AuthActions /></AuthProvider>);
    await user.click(screen.getByRole('button', { name: 'Log in' }));
    await screen.findByText('Reader / in');
    await user.click(screen.getByRole('button', { name: 'Log out' }));

    expect(screen.getByTestId('identity')).toHaveTextContent('nobody / out');
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });
});
