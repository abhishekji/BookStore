import { afterEach, describe, expect, it, vi } from 'vitest';
import { cartApi } from './client';

describe('cartApi', () => {
  afterEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('sends the authenticated token when adding a book', async () => {
    localStorage.setItem('bookstore_access_token', 'token');
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue({ id: 'cart', items: [], total: 0 }),
    }));

    await cartApi.addToCart('book-id');

    expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/cart/items', expect.objectContaining({
      method: 'POST',
      headers: expect.objectContaining({ Authorization: 'Bearer ' + 'token' }),
    }));
  });

  it('sends the authenticated token when changing quantity', async () => {
    localStorage.setItem('bookstore_access_token', 'token');
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue({ id: 'cart', items: [], total: 0 }),
    }));

    await cartApi.changeCartQuantity('book-id', 2);

    expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/cart/items/book-id', expect.objectContaining({
      method: 'PATCH',
      headers: expect.objectContaining({ Authorization: 'Bearer ' + 'token' }),
    }));
  });

  it('rejects invalid quantities before making a request', async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal('fetch', fetchMock);

    await expect(cartApi.changeCartQuantity('book-id', 0)).rejects.toThrow('Quantity must be at least 1.');
    expect(fetchMock).not.toHaveBeenCalled();
  });
});
