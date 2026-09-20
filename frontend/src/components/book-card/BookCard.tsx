import type { Book } from '../../domain/types/types';
const PRICE_DECIMAL_PLACES = 2;
export function BookCard({ book }: { book: Book }) {
  return <article className={`book-card${book.inStock ? '' : ' book-card--unavailable'}`}>
    <h2>{book.title}</h2>
    <p>{book.author}</p>
    <strong>${book.price.toFixed(PRICE_DECIMAL_PLACES)}</strong>
    <p aria-label="Availability">{book.inStock ? 'In stock' : 'Currently unavailable'}</p>
  </article>;
}
