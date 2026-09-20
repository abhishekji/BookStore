import { useEffect, useState } from 'react';
import { bookApi } from '../../api/client/client';
import type { Book } from '../../domain/types/types';
import { BookCard } from '../../components/book-card/BookCard';
export function CataloguePage() {
  const [books, setBooks] = useState<Book[]>([]);
  const [error, setError] = useState<string>();
  const [isLoading, setIsLoading] = useState(true);
  useEffect(() => {
    bookApi.getBooks()
      .then(setBooks)
      .catch(() => setError('Could not load the catalogue.'))
      .finally(() => setIsLoading(false));
  }, []);
  return <main>
    <h1>Bookstore catalogue</h1>
    {isLoading && <p role="status">Loading books...</p>}
    {error && <p role="alert">{error}</p>}
    {!isLoading && !error && books.length === 0 && <p>No books are currently available.</p>}
    <section className="catalogue" aria-label="Book catalogue">
      {books.map(book => <BookCard key={book.id} book={book} />)}
    </section>
  </main>;
}
