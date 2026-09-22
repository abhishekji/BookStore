import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { BookCard } from './BookCard';
import { availableBook, unavailableBook } from '../../domain/book/Book.fixture';

describe('BookCard', () => {
  it('renders book details and formatted price', () => {
    render(<BookCard book={availableBook} />);

    expect(screen.getByRole('heading', { name: 'Clean Code' })).toBeInTheDocument();
    expect(screen.getByText('Robert Martin')).toBeInTheDocument();
    expect(screen.getByText('$39.99')).toBeInTheDocument();
    expect(screen.getByLabelText('Availability')).toHaveTextContent('In stock');
  });

  it('represents an unavailable book', () => {
    render(<BookCard book={unavailableBook} />);

    expect(screen.getByLabelText('Availability')).toHaveTextContent('Currently unavailable');
    expect(screen.getByRole('article')).toHaveClass('book-card--unavailable');
  });

  it('shows the cart action and invokes it for an available book', async () => {
    const onAdd = vi.fn();
    render(<BookCard book={availableBook} onAdd={onAdd} />);

    await userEvent.click(screen.getByRole('button', { name: 'Add to cart' }));

    expect(onAdd).toHaveBeenCalledWith(availableBook.id);
  });

  it('can render a go-to-cart action after the book is added', () => {
    render(<BookCard book={availableBook} onAdd={vi.fn()} actionLabel="Go to cart" />);

    expect(screen.getByRole('button', { name: 'Go to cart' })).toBeInTheDocument();
  });
});
