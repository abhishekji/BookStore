import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Provider } from 'react-redux';
import { act } from 'react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { App } from './App';
import { clearCart, hideToast, store } from './state/store';
import { cartApi, TOKEN_KEY, type Cart } from './api/client/client';
import { installFakeIntersectionObserver } from './test/intersection-observer';
import { availableBook } from './domain/book/Book.fixture';

vi.mock('./api/client/client', async () => {
  const actual = await vi.importActual<typeof import('./api/client/client')>('./api/client/client');
  return {
    ...actual,
    bookApi: { getBooks: vi.fn() },
    cartApi: { ...actual.cartApi, getCart: vi.fn(), addToCart: vi.fn(), changeCartQuantity: vi.fn(), removeFromCart: vi.fn() },
    orderApi: { checkout: vi.fn(), getOrders: vi.fn(), getOrder: vi.fn() },
  };
});

const { bookApi } = await import('./api/client/client');

const cart: Cart = {
  id: 'cart-1', total: 39.99,
  items: [{ bookId: 'book-1', title: 'Clean Code', quantity: 2, unitPrice: 19.995, lineTotal: 39.99 }],
};

const renderApp = () => render(<Provider store={store}><App /></Provider>);
const signIn = () => localStorage.setItem(TOKEN_KEY, 'token');

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    installFakeIntersectionObserver();
    localStorage.clear();
    store.dispatch(clearCart());
    store.dispatch(hideToast());
    window.history.pushState({}, '', '/books');
    vi.mocked(bookApi.getBooks).mockResolvedValue({ content: [], offset: 0, limit: 5, hasNext: false, total: 0 });
    vi.mocked(cartApi.getCart).mockResolvedValue(cart);
  });
  afterEach(() => {
    vi.unstubAllGlobals();
    localStorage.clear();
  });

  it('composes authentication inside the application boundary', () => {
    renderApp();
    expect(screen.getByRole('button', { name: /log in/i })).toBeInTheDocument();
  });

  it('switches to registration and records the route', async () => {
    const user = userEvent.setup();

    renderApp();
    await user.click(screen.getByRole('button', { name: /create an account/i }));

    expect(screen.getByRole('button', { name: /^create account$/i })).toBeInTheDocument();
    expect(window.location.pathname).toBe('/register');
  });

  it('shows the catalogue and the cart count to a signed-in reader', async () => {
    signIn();

    renderApp();

    expect(await screen.findByRole('heading', { name: 'Bookstore catalogue' })).toBeInTheDocument();
    expect(await screen.findByRole('button', { name: /^cart 2$/i })).toBeInTheDocument();
  });

  it('adds a book from the catalogue and updates the cart count in the header', async () => {
    signIn();
    vi.mocked(bookApi.getBooks).mockResolvedValue({ content: [availableBook], offset: 0, limit: 5, hasNext: false, total: 1 });
    vi.mocked(cartApi.getCart).mockResolvedValue({ id: 'cart-1', total: 0, items: [] });
    vi.mocked(cartApi.addToCart).mockResolvedValue(cart);
    const user = userEvent.setup();

    renderApp();
    await user.click(await screen.findByRole('button', { name: 'Add to cart' }));

    expect(cartApi.addToCart).toHaveBeenCalledWith(availableBook.id);
    expect(await screen.findByRole('button', { name: /^cart 2$/i })).toBeInTheDocument();
  });

  it('follows browser navigation between the login and registration forms', () => {
    renderApp();

    act(() => {
      window.history.pushState({}, '', '/register');
      window.dispatchEvent(new PopStateEvent('popstate'));
    });

    expect(screen.getByRole('button', { name: /^create account$/i })).toBeInTheDocument();
  });

  it('navigates to the cart and records the route in browser history', async () => {
    signIn();
    const user = userEvent.setup();

    renderApp();
    await user.click(await screen.findByRole('button', { name: /^cart/i }));

    expect(await screen.findByRole('heading', { name: 'Your cart' })).toBeInTheDocument();
    expect(window.location.pathname).toBe('/cart');
  });

  it('returns to the catalogue from the brand and the books tab', async () => {
    signIn();
    const user = userEvent.setup();

    renderApp();
    await user.click(await screen.findByRole('button', { name: /^cart/i }));
    await screen.findByRole('heading', { name: 'Your cart' });

    await user.click(screen.getByRole('button', { name: /book\s*hub/i }));
    expect(await screen.findByRole('heading', { name: 'Bookstore catalogue' })).toBeInTheDocument();
    expect(window.location.pathname).toBe('/books');

    await user.click(screen.getByRole('button', { name: /^cart/i }));
    await screen.findByRole('heading', { name: 'Your cart' });
    await user.click(screen.getByRole('button', { name: 'Books' }));
    expect(await screen.findByRole('heading', { name: 'Bookstore catalogue' })).toBeInTheDocument();
  });

  it('follows browser back navigation between pages', async () => {
    signIn();
    const user = userEvent.setup();

    renderApp();
    await user.click(await screen.findByRole('button', { name: /^cart/i }));
    await screen.findByRole('heading', { name: 'Your cart' });

    act(() => {
      window.history.pushState({}, '', '/books');
      window.dispatchEvent(new PopStateEvent('popstate'));
    });

    expect(await screen.findByRole('heading', { name: 'Bookstore catalogue' })).toBeInTheDocument();
  });

  it('walks the reader from the cart through checkout to confirmation', async () => {
    signIn();
    const { orderApi } = await import('./api/client/client');
    vi.mocked(orderApi.checkout).mockResolvedValue({
      id: 'order-9', total: 39.99, status: 'CONFIRMED', createdAt: '2026-09-21T10:15:00Z',
      items: [{ bookId: 'book-1', bookTitle: 'Clean Code', quantity: 2, unitPrice: 19.995, lineTotal: 39.99 }],
    });
    const user = userEvent.setup();

    renderApp();
    await user.click(await screen.findByRole('button', { name: /^cart/i }));
    await user.click(await screen.findByRole('button', { name: /proceed to checkout/i }));
    await user.click(await screen.findByRole('button', { name: 'Place Order' }));

    expect(await screen.findByRole('heading', { name: /thank you for your order/i })).toBeInTheDocument();
    expect(screen.getByText('order-9')).toBeInTheDocument();
  });

  it('returns the reader to the login form and clears the cart on logout', async () => {
    signIn();
    const user = userEvent.setup();

    renderApp();
    await screen.findByRole('heading', { name: 'Bookstore catalogue' });
    await user.click(screen.getByRole('button', { name: 'Log out' }));

    expect(await screen.findByRole('button', { name: /log in/i })).toBeInTheDocument();
    expect(store.getState().cart.cart).toBeUndefined();
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });

  it('reports a cart that cannot be loaded without blocking the catalogue', async () => {
    signIn();
    vi.mocked(cartApi.getCart).mockRejectedValue(new Error('Cart service unavailable'));

    renderApp();

    expect(await screen.findByRole('heading', { name: 'Bookstore catalogue' })).toBeInTheDocument();
    await waitFor(() => expect(store.getState().cart.error).toBe('Cart service unavailable'));
  });
});
