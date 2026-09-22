import * as Sentry from '@sentry/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { initializeObservability } from './sentry';

vi.mock('@sentry/react', () => ({ init: vi.fn() }));

describe('frontend observability', () => {
  beforeEach(() => vi.mocked(Sentry.init).mockClear());
  afterEach(() => vi.unstubAllEnvs());

  it('sends no telemetry when no DSN is configured', () => {
    vi.stubEnv('VITE_SENTRY_DSN', '');
    initializeObservability();
    expect(Sentry.init).not.toHaveBeenCalled();
  });

  it('initializes Sentry once a DSN is configured', () => {
    vi.stubEnv('VITE_SENTRY_DSN', 'https://public@sentry.example.com/1');
    vi.stubEnv('VITE_RELEASE', 'bookstore-frontend@1.2.3');
    initializeObservability();
    expect(Sentry.init).toHaveBeenCalledWith(expect.objectContaining({
      dsn: 'https://public@sentry.example.com/1',
      release: 'bookstore-frontend@1.2.3',
    }));
  });

  it('never opts in to default personal data and samples a fraction of traces', () => {
    vi.stubEnv('VITE_SENTRY_DSN', 'https://public@sentry.example.com/1');
    initializeObservability();
    expect(Sentry.init).toHaveBeenCalledWith(expect.objectContaining({
      sendDefaultPii: false,
      tracesSampleRate: 0.1,
      environment: import.meta.env.MODE,
    }));
  });
});
