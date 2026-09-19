import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { BookCard } from './BookCard';

describe('BookCard', () => {
  it('renders book details and formatted price', () => {
    render(<BookCard book={{
      id: '1',
      title: 'Clean Code',
      author: 'Robert Martin',
      price: 39.9,
      stockQuantity: 2,
    }} />);

    expect(screen.getByRole('heading', { name: 'Clean Code' })).toBeInTheDocument();
    expect(screen.getByText('Robert Martin')).toBeInTheDocument();
    expect(screen.getByText('$39.90')).toBeInTheDocument();
  });
});
