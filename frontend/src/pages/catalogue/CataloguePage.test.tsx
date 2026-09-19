import { render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CataloguePage } from './CataloguePage';
import { getBooks } from '../../api/client/client';

vi.mock('../../api/client/client', () => ({ getBooks: vi.fn() }));

const mockedGetBooks = vi.mocked(getBooks);

describe('CataloguePage', () => {
  beforeEach(() => vi.resetAllMocks());

  it('renders the loaded catalogue', async () => {
    mockedGetBooks.mockResolvedValue([
      { id: '1', title: 'Clean Code', author: 'Robert Martin', price: 39.99, stockQuantity: 3 },
    ]);

    render(<CataloguePage />);

    expect(screen.getByRole('heading', { name: 'Bookstore catalogue' })).toBeInTheDocument();
    await waitFor(() => expect(screen.getByText('Clean Code')).toBeInTheDocument());
  });

  it('renders an error when catalogue loading fails', async () => {
    mockedGetBooks.mockRejectedValue(new Error('network failure'));

    render(<CataloguePage />);

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not load the catalogue.');
  });
});
