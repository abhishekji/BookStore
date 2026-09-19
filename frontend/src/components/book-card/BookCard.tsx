import type { Book } from '../../domain/types/types';
export function BookCard({ book }: { book: Book }) {
  return <article className="book-card"><h2>{book.title}</h2><p>{book.author}</p><strong>${book.price.toFixed(2)}</strong></article>;
}
