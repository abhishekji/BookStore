import { useCallback, useEffect, useRef, useState } from 'react';
import { bookApi } from '../../api/client/client';
import type { Book } from '../../domain/types/types';
import { BookCard } from '../../components/book-card/BookCard';
import './CataloguePage.css';
import { useAppDispatch, useAppSelector } from '../../state/hooks';
import { markBookSelected, showToast } from '../../state/store';
import { SearchInput } from '../../components/search-input/SearchInput';
import { InfiniteScroll } from '../../components/infinite-scroll/InfiniteScroll';
export function CataloguePage({ onAddToCart, onGoToCart }: {
  onAddToCart?: (bookId: string) => Promise<void>;
  onGoToCart?: () => void;
} = {}) {
  const [books, setBooks] = useState<Book[]>([]);
  const [search, setSearch] = useState('');
  const [query, setQuery] = useState('');
  const [offset, setOffset] = useState(0);
  const [hasNext, setHasNext] = useState(true);
  const [addingBookId, setAddingBookId] = useState<string>();
  const [isLoading, setIsLoading] = useState(true);
  const requestId = useRef(0);
  const loadingRef = useRef(false);
  const dispatch = useAppDispatch();
  const addedBookIds = useAppSelector(state => state.catalogue.selectedBookIds);
  useEffect(() => {
    const timeout = window.setTimeout(() => setQuery(search.trim()), 300);
    return () => window.clearTimeout(timeout);
  }, [search]);
  useEffect(() => {
    const currentRequest = ++requestId.current;
    setBooks([]);
    setIsLoading(true);
    loadingRef.current = true;
    setOffset(0);
    bookApi.getBooks(0, 5, query)
      .then(page => {
        if (currentRequest !== requestId.current) return;
        const content = Array.isArray(page.content) ? page.content : [];
        setBooks([...new Map(content.map(book => [book.id, book])).values()]);
        setHasNext(page.hasNext === true);
      })
      .catch(() => dispatch(showToast({ type: 'error', message: 'Could not load the catalogue.' })))
      .finally(() => {
        if (currentRequest === requestId.current) {
          setIsLoading(false);
          loadingRef.current = false;
        }
      });
  }, [query, dispatch]);
  const loadMore = useCallback(async () => {
    if (loadingRef.current || !hasNext) return;
    const currentRequest = requestId.current;
    const nextOffset = offset + 5;
    loadingRef.current = true;
    setIsLoading(true);
    try {
      const page = await bookApi.getBooks(nextOffset, 5, query);
      if (currentRequest !== requestId.current) return;
      setBooks(current => {
        const merged = new Map(current.map(book => [book.id, book]));
        for (const book of Array.isArray(page.content) ? page.content : []) merged.set(book.id, book);
        return [...merged.values()];
      });
      setOffset(page.offset);
      setHasNext(page.hasNext === true);
    } catch {
      if (currentRequest === requestId.current) {
        dispatch(showToast({ type: 'error', message: 'Could not load more books.' }));
      }
    } finally {
      if (currentRequest === requestId.current) {
        setIsLoading(false);
        loadingRef.current = false;
      }
    }
  }, [dispatch, hasNext, offset, query]);
  const addBook = async (bookId: string) => {
    setAddingBookId(bookId);
    try {
      await onAddToCart?.(bookId);
      dispatch(markBookSelected(bookId));
    } catch (addError) {
      dispatch(showToast({ type: 'error', message: addError instanceof Error ? addError.message : 'Unable to add this book to your cart.' }));
    } finally {
      setAddingBookId(undefined);
    }
  };
  return <main className="catalogue-page">
    <div className="page-heading"><div><p className="section-kicker">Discover your next favourite</p><h1>Bookstore catalogue</h1></div><SearchInput value={search} onChange={setSearch} /></div>
    {isLoading && books.length === 0 && <p role="status">Loading books...</p>}
    {!isLoading && books.length === 0 && <p>No books are currently available.</p>}
    <section className="catalogue" aria-label="Book catalogue">
      {books.map(book => <BookCard key={book.id} book={book}
        onAdd={addedBookIds.includes(book.id) ? onGoToCart : addBook}
        actionLabel={addingBookId === book.id ? 'Adding...' : addedBookIds.includes(book.id) ? 'Go to cart' : 'Add to cart'} />)}
    </section>
    {isLoading && books.length > 0 && <p role="status">Loading more books...</p>}
    <InfiniteScroll hasNext={hasNext} isLoading={isLoading} onLoadMore={loadMore} />
  </main>;
}
