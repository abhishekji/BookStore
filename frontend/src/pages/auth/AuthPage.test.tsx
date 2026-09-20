import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { AuthPage } from './AuthPage';
import { AuthProvider } from '../../application/auth-state/AuthState';
import { authApi } from '../../api/client/client';
import { Provider } from 'react-redux';
import { store } from '../../state/store';
import { Toast } from '../../components/toast/Toast';

vi.mock('../../api/client/client', async () => {
  const actual = await vi.importActual<typeof import('../../api/client/client')>('../../api/client/client');
  return { ...actual, authApi: { login: vi.fn(), register: vi.fn() } };
});

describe('AuthPage', () => {
  it('returns to login mode after successful registration', async () => {
    vi.mocked(authApi.register).mockResolvedValue({ token: 'token', email: 'reader@example.com', displayName: 'Reader' });
    const user = userEvent.setup();
    render(<Provider store={store}><Toast /><AuthProvider><AuthPage /></AuthProvider></Provider>);
    await user.click(screen.getByRole('button', { name: 'Create an account' }));
    await user.type(screen.getByLabelText('Name'), 'Reader');
    await user.type(screen.getByLabelText('Email'), 'reader@example.com');
    await user.type(screen.getByLabelText('Password'), 'StrongPass1');
    await user.click(screen.getByRole('button', { name: 'Create account' }));
    expect(await screen.findByRole('status')).toHaveTextContent('Registration successful');
    expect(screen.getByRole('heading', { name: 'Welcome back' })).toBeInTheDocument();
  });
});
