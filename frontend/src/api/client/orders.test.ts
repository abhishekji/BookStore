import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { orderApi, TOKEN_KEY, type Order, type OrderPage } from './client';

const order: Order = {
  id: 'order-1', status: 'CONFIRMED', createdAt: '2026-09-21T10:15:00Z', total: 39.99,
  items: [{ bookId: 'book-1', bookTitle: 'Clean Code', quantity: 1, unitPrice: 39.99, lineTotal: 39.99 }],
};
const page: OrderPage = { content: [order], offset: 0, limit: 10, hasNext: false, total: 1 };

const okResponse = (body: unknown) => ({ ok: true, status: 200, json: vi.fn().mockResolvedValue(body) });

describe('orderApi', () => {
  beforeEach(() => localStorage.setItem(TOKEN_KEY, 'token'));
  afterEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('checks out with the caller-supplied idempotency key', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(okResponse(order)));

    await expect(orderApi.checkout('attempt-1')).resolves.toEqual(order);

    expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/orders/checkout', expect.objectContaining({
      method: 'POST',
      headers: expect.objectContaining({ 'Idempotency-Key': 'attempt-1', Authorization: 'Bearer token' }),
    }));
  });

  it('lists the authenticated reader\'s orders', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(okResponse(page)));

    await expect(orderApi.getOrders()).resolves.toEqual(page);
    expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/orders?offset=0&limit=10', expect.anything());
  });

  it('retrieves a single order by identifier', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(okResponse(order)));

    await expect(orderApi.getOrder('order-1')).resolves.toEqual(order);
    expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/orders/order-1', expect.anything());
  });

  it('surfaces the backend error message when checkout is rejected', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false, status: 409,
      json: vi.fn().mockResolvedValue({ message: 'Checkout is already in progress' }),
    }));

    await expect(orderApi.checkout('attempt-1')).rejects.toThrow('Checkout is already in progress');
  });
});
