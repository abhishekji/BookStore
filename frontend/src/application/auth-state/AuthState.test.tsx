import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider, useAuth } from './AuthState';

function AuthConsumer() {
  const { isAuthenticated, setAuthenticated } = useAuth();
  return <button onClick={() => setAuthenticated(true)}>
    {isAuthenticated ? 'authenticated' : 'anonymous'}
  </button>;
}

describe('AuthState', () => {
  afterEach(() => vi.restoreAllMocks());

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
});
