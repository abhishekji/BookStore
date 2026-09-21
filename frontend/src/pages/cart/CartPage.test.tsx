import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { CartPage } from './CartPage';
import { AuthProvider } from '../../application/auth-state/AuthState';
import { cartApi, TOKEN_KEY, type Cart } from '../../api/client/client';
import { Provider } from 'react-redux';
import { clearCart, hideToast, store } from '../../state/store';
import { Toast } from '../../components/toast/Toast';

vi.mock('../../api/client/client', async () => {
  const actual = await vi.importActual<typeof import('../../api/client/client')>('../../api/client/client');
  return {
    ...actual,
    cartApi: { ...actual.cartApi, getCart: vi.fn(), changeCartQuantity: vi.fn(), removeFromCart: vi.fn() },
  };
});

const cart: Cart = {
  id: 'cart', total: 39.99,
  items: [{ bookId: 'book', title: 'Clean Code', quantity: 1, unitPrice: 39.99, lineTotal: 39.99 }],
};
const emptyCart: Cart = { id: 'cart', total: 0, items: [] };
const renderPage = (props: Parameters<typeof CartPage>[0] = {}) =>
  render(<Provider store={store}><Toast /><AuthProvider><CartPage {...props} /></AuthProvider></Provider>);

describe('CartPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    store.dispatch(clearCart());
    store.dispatch(hideToast());
    localStorage.setItem(TOKEN_KEY, 'token');
  });
  afterEach(() => localStorage.clear());

  it('renders cart quantities and total', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(cart);

    renderPage();

    expect(await screen.findByText('Clean Code')).toBeInTheDocument();
    expect(screen.getAllByText('$39.99')).toHaveLength(2);
    expect(screen.getByRole('button', { name: /increase clean code quantity/i })).toBeInTheDocument();
  });

  it('cannot decrease below the last remaining copy', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(cart);

    renderPage();

    expect(await screen.findByRole('button', { name: /decrease clean code quantity/i })).toBeDisabled();
  });

  it('increases a quantity and publishes the updated cart', async () => {
    const updated: Cart = { id: 'cart', total: 79.98, items: [{ ...cart.items[0], quantity: 2, lineTotal: 79.98 }] };
    vi.mocked(cartApi.getCart).mockResolvedValue(cart);
    vi.mocked(cartApi.changeCartQuantity).mockResolvedValue(updated);
    const onCartChange = vi.fn();
    const user = userEvent.setup();

    renderPage({ onCartChange });
    await user.click(await screen.findByRole('button', { name: /increase clean code quantity/i }));

    expect(cartApi.changeCartQuantity).toHaveBeenCalledWith('book', 2);
    expect(await screen.findByLabelText('Clean Code quantity')).toHaveTextContent('2');
    await waitFor(() => expect(onCartChange).toHaveBeenCalledWith(updated));
  });

  it('reports a rejected quantity change', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(cart);
    vi.mocked(cartApi.changeCartQuantity).mockRejectedValue(new Error('Only 1 copy is available.'));
    const user = userEvent.setup();

    renderPage();
    await user.click(await screen.findByRole('button', { name: /increase clean code quantity/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Only 1 copy is available.');
  });

  it('removes an item and reloads the cart', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValueOnce(cart).mockResolvedValueOnce(emptyCart);
    vi.mocked(cartApi.removeFromCart).mockResolvedValue(undefined);
    const user = userEvent.setup();

    renderPage();
    await user.click(await screen.findByRole('button', { name: 'Remove' }));

    expect(cartApi.removeFromCart).toHaveBeenCalledWith('book');
    expect(await screen.findByRole('heading', { name: /your cart is empty/i })).toBeInTheDocument();
  });

  it('reports a failed removal and keeps the item visible', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(cart);
    vi.mocked(cartApi.removeFromCart).mockRejectedValue(new Error('Cart item not found'));
    const user = userEvent.setup();

    renderPage();
    await user.click(await screen.findByRole('button', { name: 'Remove' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Cart item not found');
    expect(screen.getByRole('button', { name: /retry loading cart/i })).toBeInTheDocument();
  });

  it('offers a route back to the catalogue from an empty cart', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(emptyCart);
    const onContinueShopping = vi.fn();
    const user = userEvent.setup();

    renderPage({ onContinueShopping });
    await user.click(await screen.findByRole('button', { name: /continue shopping/i }));

    expect(onContinueShopping).toHaveBeenCalledTimes(1);
  });

  it('starts checkout from a cart with items', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(cart);
    const onCheckout = vi.fn();
    const user = userEvent.setup();

    renderPage({ onCheckout });
    await user.click(await screen.findByRole('button', { name: /proceed to checkout/i }));

    expect(onCheckout).toHaveBeenCalledTimes(1);
  });

  it('offers a retry when the cart cannot be loaded', async () => {
    vi.mocked(cartApi.getCart).mockRejectedValueOnce(new Error('Service unavailable')).mockResolvedValueOnce(cart);
    const user = userEvent.setup();

    renderPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('Service unavailable');
    expect(store.getState().cart.error).toBe('Service unavailable');
    await user.click(screen.getByRole('button', { name: /retry loading cart/i }));
    expect(await screen.findByText('Clean Code')).toBeInTheDocument();
  });

  it('shows nothing but a warning to a signed-out reader', async () => {
    localStorage.clear();

    renderPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('Please log in to access your cart.');
    expect(screen.queryByRole('heading', { name: /your cart/i })).not.toBeInTheDocument();
    expect(cartApi.getCart).not.toHaveBeenCalled();
  });
});
