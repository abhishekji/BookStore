import { afterEach, describe, expect, it, vi } from 'vitest';
import { getBooks } from './client';

describe('getBooks', () => {
  afterEach(() => vi.restoreAllMocks());

  it('returns books from a successful response', async () => {
    const books = [{ id: '1', title: 'Clean Code', author: 'Robert Martin', price: 39.99, stockQuantity: 3, inStock: true }];
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(books),
    }));

    await expect(getBooks()).resolves.toEqual(books);
    expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/books');
  });

  it('throws a meaningful error for an unsuccessful response', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: false }));

    await expect(getBooks()).rejects.toThrow('Unable to load books');
  });
});
