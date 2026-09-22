import { render } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { InfiniteScroll } from './InfiniteScroll';
import { installFakeIntersectionObserver, observers } from '../../test/intersection-observer';

describe('InfiniteScroll', () => {
  beforeEach(() => installFakeIntersectionObserver());
  afterEach(() => vi.unstubAllGlobals());

  it('loads more once the sentinel scrolls into view', () => {
    const onLoadMore = vi.fn();
    render(<InfiniteScroll hasNext isLoading={false} onLoadMore={onLoadMore} />);
    expect(observers()).toHaveLength(1);

    observers()[0].intersect();

    expect(onLoadMore).toHaveBeenCalledTimes(1);
  });

  it('ignores a sentinel that is not intersecting', () => {
    const onLoadMore = vi.fn();
    render(<InfiniteScroll hasNext isLoading={false} onLoadMore={onLoadMore} />);

    observers()[0].intersect(false);

    expect(onLoadMore).not.toHaveBeenCalled();
  });

  it('observes ahead of the viewport so the next page arrives before the reader reaches the end', () => {
    render(<InfiniteScroll hasNext isLoading={false} onLoadMore={() => {}} />);

    expect(observers()[0].options?.rootMargin).toBe('240px');
    expect(observers()[0].observed).toHaveLength(1);
  });

  it('does not observe when there is no further page', () => {
    render(<InfiniteScroll hasNext={false} isLoading={false} onLoadMore={() => {}} />);
    expect(observers()).toHaveLength(0);
  });

  it('does not observe while a page is already loading', () => {
    render(<InfiniteScroll hasNext isLoading onLoadMore={() => {}} />);
    expect(observers()).toHaveLength(0);
  });

  it('disconnects the observer when unmounted', () => {
    const { unmount } = render(<InfiniteScroll hasNext isLoading={false} onLoadMore={() => {}} />);

    unmount();

    expect(observers()[0].disconnected).toBe(true);
  });

  it('stays inert in an environment without IntersectionObserver', () => {
    vi.stubGlobal('IntersectionObserver', undefined);
    const onLoadMore = vi.fn();

    expect(() => render(<InfiniteScroll hasNext isLoading={false} onLoadMore={onLoadMore} />)).not.toThrow();
    expect(onLoadMore).not.toHaveBeenCalled();
  });
});
