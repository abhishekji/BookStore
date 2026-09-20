import { useEffect, useRef } from 'react';

export function InfiniteScroll({ hasNext, isLoading, onLoadMore }: {
  hasNext: boolean;
  isLoading: boolean;
  onLoadMore: () => void;
}) {
  const sentinel = useRef<HTMLDivElement>(null);
  useEffect(() => {
    const element = sentinel.current;
    if (!element || !hasNext || isLoading || typeof IntersectionObserver === 'undefined') return;
    const observer = new IntersectionObserver(entries => {
      if (entries[0]?.isIntersecting) onLoadMore();
    }, { rootMargin: '240px' });
    observer.observe(element);
    return () => observer.disconnect();
  }, [hasNext, isLoading, onLoadMore]);
  return <div ref={sentinel} aria-hidden="true" />;
}
