import { vi } from 'vitest';

type ObserverCallback = (entries: { isIntersecting: boolean }[]) => void;

/**
 * jsdom has no IntersectionObserver, so infinite scrolling is never triggered by default.
 * This fake records the observers a component creates and lets a test drive intersection explicitly.
 */
export class FakeIntersectionObserver {
  static instances: FakeIntersectionObserver[] = [];
  readonly observed: Element[] = [];
  disconnected = false;

  constructor(private readonly callback: ObserverCallback, readonly options?: IntersectionObserverInit) {
    FakeIntersectionObserver.instances.push(this);
  }

  observe(element: Element) { this.observed.push(element); }
  unobserve() { /* the components under test disconnect instead */ }
  disconnect() { this.disconnected = true; }
  intersect(isIntersecting = true) { this.callback([{ isIntersecting }]); }
}

/** Installs the fake for the current test and clears any observers recorded earlier. */
export function installFakeIntersectionObserver() {
  FakeIntersectionObserver.instances = [];
  vi.stubGlobal('IntersectionObserver', FakeIntersectionObserver);
  return FakeIntersectionObserver.instances;
}

export const observers = () => FakeIntersectionObserver.instances;

/** Drives the most recently created observer, i.e. the sentinel currently mounted. */
export const latestObserver = (): FakeIntersectionObserver | undefined =>
  FakeIntersectionObserver.instances[FakeIntersectionObserver.instances.length - 1];
