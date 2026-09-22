import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Provider } from 'react-redux';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CheckoutPage } from './CheckoutPage';
import { cartLoaded, store } from '../../state/store';
import { cartApi, orderApi, type Order } from '../../api/client/client';

vi.mock('../../api/client/client', () => ({
  cartApi: { getCart: vi.fn() },
  orderApi: { checkout: vi.fn() },
}));

const cart = { id: 'cart-1', total: 20, items: [{ bookId: 'book-1', title: 'Clean Code', quantity: 2, unitPrice: 10, lineTotal: 20 }] };
const order = { id: 'order-1', total: 20, items: [], status: 'CONFIRMED', createdAt: '2026-09-21T10:15:00Z' };
const renderPage = (handlers: { onBack?: () => void; onConfirmed?: (order: Order) => void } = {}) =>
  render(<Provider store={store}><CheckoutPage onBack={handlers.onBack ?? vi.fn()} onConfirmed={handlers.onConfirmed ?? vi.fn()} /></Provider>);

describe('CheckoutPage', () => {
  beforeEach(() => { vi.clearAllMocks(); vi.stubGlobal('crypto', { randomUUID: () => 'checkout-key' }); });

  it('shows the order under review before it is placed', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(cart);
    renderPage();
    expect(screen.getByRole('status')).toHaveTextContent('Loading checkout...');
    expect(await screen.findByText('Clean Code')).toBeInTheDocument();
    expect(screen.getByText('2 × $10.00')).toBeInTheDocument();
    expect(screen.getAllByText('$20.00')).toHaveLength(3);
  });

  it('confirms the order and clears the cart once checkout succeeds', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(cart);
    vi.mocked(orderApi.checkout).mockResolvedValue(order);
    store.dispatch(cartLoaded(cart));
    const onConfirmed = vi.fn();
    const user = userEvent.setup();

    renderPage({ onConfirmed });
    await screen.findByText('Clean Code');
    await user.click(screen.getByRole('button', { name: 'Place Order' }));

    await waitFor(() => expect(onConfirmed).toHaveBeenCalledWith(order));
    expect(store.getState().cart.cart).toBeUndefined();
    expect(store.getState().toast).toEqual({ type: 'success', message: 'Your order has been placed.' });
  });

  it('returns the reader to the cart', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(cart);
    const onBack = vi.fn();
    const user = userEvent.setup();

    renderPage({ onBack });
    await screen.findByText('Clean Code');
    await user.click(screen.getByRole('button', { name: /back to cart/i }));

    expect(onBack).toHaveBeenCalledTimes(1);
  });

  it('offers a retry when the checkout summary cannot be loaded', async () => {
    vi.mocked(cartApi.getCart).mockRejectedValueOnce(new Error('Network unavailable')).mockResolvedValueOnce(cart);
    const user = userEvent.setup();
    renderPage();
    expect(await screen.findByRole('alert')).toHaveTextContent('Network unavailable');
    await user.click(screen.getByRole('button', { name: /retry loading checkout/i }));
    expect(await screen.findByText('Clean Code')).toBeInTheDocument();
  });

  it('keeps the idempotency key when an order retry follows a failure', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(cart);
    vi.mocked(orderApi.checkout).mockRejectedValueOnce(new Error('Timed out')).mockResolvedValueOnce({ id: 'order-1', total: 20, items: [], status: 'CONFIRMED', createdAt: '' });
    const user = userEvent.setup();
    renderPage();
    await screen.findByText('Clean Code');
    await user.click(screen.getByRole('button', { name: 'Place Order' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Timed out');
    await user.click(screen.getByRole('button', { name: 'Place Order' }));
    await waitFor(() => expect(orderApi.checkout).toHaveBeenCalledTimes(2));
    expect(orderApi.checkout).toHaveBeenNthCalledWith(1, 'checkout-key');
    expect(orderApi.checkout).toHaveBeenNthCalledWith(2, 'checkout-key');
  });
});
