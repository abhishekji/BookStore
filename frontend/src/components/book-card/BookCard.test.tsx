import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { BookCard } from './BookCard';
import { availableBook, unavailableBook } from '../../test/fixtures/books';

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
});
