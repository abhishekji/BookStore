import { useEffect, useState } from 'react';
import { getBooks } from '../../api/client/client';
import type { Book } from '../../domain/types/types';
import { BookCard } from '../../components/book-card/BookCard';
export function CataloguePage() {
  const [books, setBooks] = useState<Book[]>([]);
  const [error, setError] = useState<string>();
  useEffect(() => { getBooks().then(setBooks).catch(() => setError('Could not load the catalogue.')); }, []);
  return <main><h1>Bookstore catalogue</h1>{error && <p role="alert">{error}</p>}<section className="catalogue">{books.map(book => <BookCard key={book.id} book={book} />)}</section></main>;
}
