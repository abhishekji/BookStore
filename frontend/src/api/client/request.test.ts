import { afterEach, describe, expect, it, vi } from 'vitest';
import { authApi, cartApi, getBooks, TOKEN_KEY } from './client';

const book = { id: '1', title: 'Clean Code', author: 'Robert Martin', isbn: '9780132350884', price: 39.99, stockQuantity: 3, inStock: true };

describe('API request handling', () => {
  afterEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('omits the authorization header when nobody is signed in', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, status: 200, json: vi.fn().mockResolvedValue([]) }));

    await getBooks();

    const [, options] = vi.mocked(fetch).mock.calls[0];
    expect(options?.headers).toEqual({ 'Content-Type': 'application/json' });
  });

  it('discards an expired token when the backend rejects it', async () => {
    localStorage.setItem(TOKEN_KEY, 'expired');
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false, status: 401,
      json: vi.fn().mockResolvedValue({ message: 'Authentication is required to access this resource' }),
    }));

    await expect(cartApi.getCart()).rejects.toThrow('Authentication is required to access this resource');
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });

  it('falls back to the status code when the error body is not JSON', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false, status: 503,
      json: vi.fn().mockRejectedValue(new SyntaxError('Unexpected token')),
    }));

    await expect(cartApi.getCart()).rejects.toThrow('Request failed (503)');
  });

  it('prefers a problem-detail body over the generic status message', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false, status: 400,
      json: vi.fn().mockResolvedValue({ detail: 'quantity: must be at least 1' }),
    }));

    await expect(cartApi.getCart()).rejects.toThrow('quantity: must be at least 1');
  });

  it('returns nothing for an empty 204 response', async () => {
    const json = vi.fn();
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, status: 204, json }));

    await expect(cartApi.removeFromCart('book-1')).resolves.toBeUndefined();
    expect(json).not.toHaveBeenCalled();
  });

  it('registers and logs in against the unversioned auth routes', async () => {
    const authResponse = { token: 'jwt', email: 'reader@example.com', displayName: 'Reader' };
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, status: 200, json: vi.fn().mockResolvedValue(authResponse) }));

    await expect(authApi.register('reader@example.com', 'StrongPass1', 'Reader')).resolves.toEqual(authResponse);
    expect(fetch).toHaveBeenLastCalledWith('http://localhost:8080/api/auth/register', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ email: 'reader@example.com', password: 'StrongPass1', displayName: 'Reader' }),
    }));

    await expect(authApi.login('reader@example.com', 'StrongPass1')).resolves.toEqual(authResponse);
    expect(fetch).toHaveBeenLastCalledWith('http://localhost:8080/api/auth/login', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ email: 'reader@example.com', password: 'StrongPass1' }),
    }));
  });
});

describe('getBooks response shapes', () => {
  afterEach(() => vi.restoreAllMocks());

  it('adapts a legacy bare-array catalogue response into a page', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, status: 200, json: vi.fn().mockResolvedValue([book]) }));

    await expect(getBooks(10, 5)).resolves.toEqual({ content: [book], offset: 10, limit: 5, hasNext: false, total: 1 });
  });

  it('defaults missing page metadata to the requested window', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, status: 200, json: vi.fn().mockResolvedValue({ content: [book] }) }));

    await expect(getBooks(5, 5)).resolves.toEqual({ content: [book], offset: 5, limit: 5, hasNext: false, total: 1 });
  });

  it('tolerates a malformed content field', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, status: 200, json: vi.fn().mockResolvedValue({ content: null, offset: 0, limit: 5, hasNext: false }) }));

    await expect(getBooks()).resolves.toMatchObject({ content: [], total: 0 });
  });

  it('url-encodes the search term', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, status: 200, json: vi.fn().mockResolvedValue([]) }));

    await getBooks(0, 5, 'clean code & more');

    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/v1/books?offset=0&limit=5&search=clean%20code%20%26%20more',
      expect.anything(),
    );
  });
});
