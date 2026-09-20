import { render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CataloguePage } from './CataloguePage';
import { bookApi } from '../../api/client/client';
import { availableBook } from '../../test/fixtures/books';

vi.mock('../../api/client/client', () => ({ bookApi: { getBooks: vi.fn() } }));

const mockedGetBooks = vi.mocked(bookApi.getBooks);

describe('CataloguePage', () => {
  beforeEach(() => vi.resetAllMocks());

  it('renders the loaded catalogue', async () => {
    mockedGetBooks.mockResolvedValue([availableBook]);

    render(<CataloguePage />);

    expect(screen.getByRole('heading', { name: 'Bookstore catalogue' })).toBeInTheDocument();
    await waitFor(() => expect(screen.getByText('Clean Code')).toBeInTheDocument());
  });

  it('renders an error when catalogue loading fails', async () => {
    mockedGetBooks.mockRejectedValue(new Error('network failure'));

    render(<CataloguePage />);

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not load the catalogue.');
  });

  it('renders a loading state while the API is pending', () => {
    mockedGetBooks.mockReturnValue(new Promise(() => {}));

    render(<CataloguePage />);

    expect(screen.getByRole('status')).toHaveTextContent('Loading books...');
  });

  it('renders an empty state when no books are returned', async () => {
    mockedGetBooks.mockResolvedValue([]);

    render(<CataloguePage />);

    expect(await screen.findByText('No books are currently available.')).toBeInTheDocument();
  });
});
