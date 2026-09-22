import type { Book } from '../../domain/types/types';
import { API } from '../../config/constants';

const baseUrl = import.meta.env.VITE_API_URL ?? API.defaultUrl;
const apiRoot = baseUrl.replace(/\/v1$/, '');
export const TOKEN_KEY = 'bookstore_access_token';

export type AuthResponse = { token: string; email: string; displayName: string };
export type CartItem = { cartItemId?: string; bookId: string; title: string; quantity: number; unitPrice: number; lineTotal: number };
export type Cart = { id: string; items: CartItem[]; total: number };
export type OrderItem = { bookId: string; bookTitle: string; quantity: number; unitPrice: number; lineTotal: number };
export type Order = { id: string; items: OrderItem[]; total: number; status: string; createdAt: string; updatedAt?: string };
export type BookPage = { content: Book[]; offset: number; limit: number; hasNext: boolean; total: number };
export type OrderPage = { content: Order[]; offset: number; limit: number; hasNext: boolean; total: number };

function requestHeaders(): HeadersInit {
  const token = localStorage.getItem(TOKEN_KEY);
  return token
    ? { Authorization: 'Bearer ' + token, 'Content-Type': 'application/json' }
    : { 'Content-Type': 'application/json' };
}

function assertSecureTransport() {
  const isLocalDevelopment = window.location.hostname === 'localhost'
    || window.location.hostname === '127.0.0.1';
  if (import.meta.env.PROD && !isLocalDevelopment && !apiRoot.startsWith('https://')) {
    throw new Error('Secure HTTPS API transport is required outside local development');
  }
}

async function request<T>(path: string, options: RequestInit = {}, root = apiRoot): Promise<T> {
  assertSecureTransport();
  const response = await fetch(`${root}${path}`, {
    ...options,
    headers: { ...requestHeaders(), ...options.headers },
  });
  if (!response.ok) {
    if (response.status === 401) localStorage.removeItem(TOKEN_KEY);
    let message = `Request failed (${response.status ?? 'unknown'})`;
    try {
      const body = await response.json() as { message?: string; detail?: string };
      message = body.message ?? body.detail ?? message;
    } catch {
      // Preserve a useful status message for non-JSON error responses.
    }
    throw new Error(message);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

async function getBooks(offset = 0, limit = 5, search = ''): Promise<BookPage> {
  const response = await request<BookPage | Book[]>(
    `${API.booksResource}?offset=${offset}&limit=${limit}&search=${encodeURIComponent(search)}`,
    {},
    baseUrl,
  );
  if (Array.isArray(response)) {
    return {
      content: response,
      offset,
      limit,
      hasNext: false,
      total: response.length,
    };
  }
  return {
    content: Array.isArray(response.content) ? response.content : [],
    offset: response.offset ?? offset,
    limit: response.limit ?? limit,
    hasNext: response.hasNext === true,
    total: response.total ?? response.content?.length ?? 0,
  };
}

async function register(email: string, password: string, displayName: string): Promise<AuthResponse> {
  return request<AuthResponse>('/auth/register', {
    method: 'POST',
    body: JSON.stringify({ email, password, displayName }),
  });
}

async function login(email: string, password: string): Promise<AuthResponse> {
  return request<AuthResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
}

async function getCart(): Promise<Cart> { return request<Cart>('/cart'); }

async function addToCart(bookId: string, quantity = 1): Promise<Cart> {
  return request<Cart>('/cart/items', {
    method: 'POST',
    body: JSON.stringify({ bookId, quantity }),
  });
}

async function changeCartQuantity(bookId: string, quantity: number): Promise<Cart> {
  if (!Number.isInteger(quantity) || quantity < 1) throw new Error('Quantity must be at least 1.');
  return request<Cart>(`/cart/items/${bookId}`, {
    method: 'PATCH',
    body: JSON.stringify({ quantity }),
  });
}

async function removeFromCart(bookId: string): Promise<void> {
  return request<void>(`/cart/items/${bookId}`, { method: 'DELETE' });
}
async function checkout(idempotencyKey: string): Promise<Order> {
  return request<Order>(`${API.ordersResource}/checkout`, { method: 'POST', headers: { 'Idempotency-Key': idempotencyKey } });
}
async function getOrders(offset = 0, limit = 10): Promise<OrderPage> {
  return request<OrderPage>(`${API.ordersResource}?offset=${offset}&limit=${limit}`);
}
async function getOrder(orderId: string): Promise<Order> { return request<Order>(`${API.ordersResource}/${orderId}`); }

export const authApi = { register, login };
export const cartApi = { getCart, addToCart, changeCartQuantity, removeFromCart };
export const orderApi = { checkout, getOrders, getOrder };
export const bookApi = { getBooks };
export { getBooks };
