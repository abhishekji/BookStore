/**
 * The secure-transport guard only engages in a production bundle served from a real host,
 * so this suite runs with a non-local jsdom origin instead of the default localhost.
 *
 * @vitest-environment jsdom
 * @vitest-environment-options { "url": "https://shop.example.com/books" }
 */
import { afterEach, describe, expect, it, vi } from 'vitest';

afterEach(() => {
  vi.unstubAllEnvs();
  vi.unstubAllGlobals();
  vi.resetModules();
});

describe('secure transport guard', () => {
  it('refuses to call a plain HTTP API from a deployed frontend', async () => {
    vi.stubEnv('PROD', true);
    vi.stubEnv('VITE_API_URL', 'http://api.example.com/api/v1');
    const fetchMock = vi.fn();
    vi.stubGlobal('fetch', fetchMock);
    const { cartApi } = await import('./client');

    await expect(cartApi.getCart()).rejects.toThrow('Secure HTTPS API transport is required outside local development');
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it('allows an HTTPS API from a deployed frontend', async () => {
    vi.stubEnv('PROD', true);
    vi.stubEnv('VITE_API_URL', 'https://api.example.com/api/v1');
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, status: 200, json: vi.fn().mockResolvedValue({ id: 'cart', items: [], total: 0 }) }));
    const { cartApi } = await import('./client');

    await expect(cartApi.getCart()).resolves.toEqual({ id: 'cart', items: [], total: 0 });
    expect(fetch).toHaveBeenCalledWith('https://api.example.com/api/cart', expect.anything());
  });
});
