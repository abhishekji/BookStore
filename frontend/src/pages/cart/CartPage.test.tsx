import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { CartPage } from './CartPage';
import { AuthProvider } from '../../application/auth-state/AuthState';
import { cartApi } from '../../api/client/client';
import { Provider } from 'react-redux';
import { store } from '../../state/store';

vi.mock('../../api/client/client', async () => {
  const actual = await vi.importActual<typeof import('../../api/client/client')>('../../api/client/client');
  return { ...actual, cartApi: { ...actual.cartApi, getCart: vi.fn() } };
});

describe('CartPage', () => {
  it('renders cart quantities and total', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue({
      id: 'cart', total: 39.99,
      items: [{ bookId: 'book', title: 'Clean Code', quantity: 1, unitPrice: 39.99, lineTotal: 39.99 }],
    });
    localStorage.setItem('bookstore_access_token', 'token');
    render(<Provider store={store}><AuthProvider><CartPage /></AuthProvider></Provider>);
    expect(await screen.findByText('Clean Code')).toBeInTheDocument();
    expect(screen.getAllByText('$39.99')).toHaveLength(2);
    expect(screen.getByRole('button', { name: /increase clean code quantity/i })).toBeInTheDocument();
  });
});
