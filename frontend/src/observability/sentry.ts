import * as Sentry from '@sentry/react';

/**
 * Observability is opt-in: local development and tests do not send telemetry.
 * Configure VITE_SENTRY_DSN (and optionally VITE_RELEASE) in deployed environments.
 */
export function initializeObservability() {
  const dsn = import.meta.env.VITE_SENTRY_DSN;
  if (!dsn) return;
  Sentry.init({
    dsn,
    environment: import.meta.env.MODE,
    release: import.meta.env.VITE_RELEASE,
    tracesSampleRate: 0.1,
    sendDefaultPii: false,
  });
}
