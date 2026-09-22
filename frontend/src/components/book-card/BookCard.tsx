import type { Book } from '../../domain/types/types';
import { UI } from '../../config/constants';
import './BookCard.css';
export function BookCard({ book, onAdd, actionLabel = 'Add to cart' }: {
  book: Book; onAdd?: (bookId: string) => void; actionLabel?: string;
}) {
  return <article className={`book-card${book.inStock ? '' : ' book-card--unavailable'}`}>
    <h2>{book.title}</h2>
    <p>{book.author}</p>
    <strong>${book.price.toFixed(UI.bookPriceDecimalPlaces)}</strong>
    <p aria-label="Availability">{book.inStock ? 'In stock' : 'Currently unavailable'}</p>
    {book.inStock && onAdd && <button className="book-card__action" type="button" onClick={() => onAdd(book.id)}>{actionLabel}</button>}
  </article>;
}
