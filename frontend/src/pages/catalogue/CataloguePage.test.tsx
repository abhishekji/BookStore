import { act, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { CataloguePage } from './CataloguePage';
import { bookApi } from '../../api/client/client';
import { availableBook, unavailableBook } from '../../test/fixtures/books';
import { Provider } from 'react-redux';
import { clearCatalogue, hideToast, store } from '../../state/store';
import { Toast } from '../../components/toast/Toast';
import { installFakeIntersectionObserver, latestObserver } from '../../test/intersection-observer';

vi.mock('../../api/client/client', () => ({ bookApi: { getBooks: vi.fn() } }));

const mockedGetBooks = vi.mocked(bookApi.getBooks);
const page = (content = [availableBook], hasNext = false, offset = 0) =>
  ({ content, offset, limit: 5, hasNext, total: content.length });
const scrollToEnd = async () => act(async () => { latestObserver()?.intersect(); });

describe('CataloguePage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    installFakeIntersectionObserver();
    store.dispatch(clearCatalogue());
    store.dispatch(hideToast());
  });
  afterEach(() => vi.unstubAllGlobals());

  it('renders the loaded catalogue', async () => {
    mockedGetBooks.mockResolvedValue({ content: [availableBook], offset: 0, limit: 5, hasNext: false, total: 1 });

    render(<Provider store={store}><Toast /><CataloguePage /></Provider>);

    expect(screen.getByRole('heading', { name: 'Bookstore catalogue' })).toBeInTheDocument();
    await waitFor(() => expect(screen.getByText('Clean Code')).toBeInTheDocument());
  });

  it('renders an error when catalogue loading fails', async () => {
    mockedGetBooks.mockRejectedValue(new Error('network failure'));

    render(<Provider store={store}><Toast /><CataloguePage /></Provider>);

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not load the catalogue.');
  });

  it('renders a loading state while the API is pending', () => {
    mockedGetBooks.mockReturnValue(new Promise(() => {}));

    render(<Provider store={store}><Toast /><CataloguePage /></Provider>);

    expect(screen.getByRole('status')).toHaveTextContent('Loading books...');
  });

  it('renders an empty state when no books are returned', async () => {
    mockedGetBooks.mockResolvedValue({ content: [], offset: 0, limit: 5, hasNext: false, total: 0 });

    render(<Provider store={store}><Toast /><CataloguePage /></Provider>);

    expect(await screen.findByText('No books are currently available.')).toBeInTheDocument();
  });

  it('re-queries the catalogue with the trimmed search term after the reader stops typing', async () => {
    mockedGetBooks.mockResolvedValue(page());
    const user = userEvent.setup();

    render(<Provider store={store}><Toast /><CataloguePage /></Provider>);
    await screen.findByText('Clean Code');
    await user.type(screen.getByRole('searchbox'), '  clean  ');

    await waitFor(() => expect(mockedGetBooks).toHaveBeenLastCalledWith(0, 5, 'clean'));
  });

  it('adds a book and then offers a route to the cart', async () => {
    mockedGetBooks.mockResolvedValue(page());
    const onAddToCart = vi.fn().mockResolvedValue(undefined);
    const onGoToCart = vi.fn();
    const user = userEvent.setup();

    render(<Provider store={store}><Toast /><CataloguePage onAddToCart={onAddToCart} onGoToCart={onGoToCart} /></Provider>);
    await user.click(await screen.findByRole('button', { name: 'Add to cart' }));

    expect(onAddToCart).toHaveBeenCalledWith(availableBook.id);
    await user.click(await screen.findByRole('button', { name: 'Go to cart' }));
    expect(onGoToCart).toHaveBeenCalledWith(availableBook.id);
  });

  it('reports a failure to add a book without losing the catalogue', async () => {
    mockedGetBooks.mockResolvedValue(page());
    const onAddToCart = vi.fn().mockRejectedValue(new Error('Only 3 copies are available.'));
    const user = userEvent.setup();

    render(<Provider store={store}><Toast /><CataloguePage onAddToCart={onAddToCart} /></Provider>);
    await user.click(await screen.findByRole('button', { name: 'Add to cart' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Only 3 copies are available.');
    expect(screen.getByText('Clean Code')).toBeInTheDocument();
  });

  it('does not offer an add action for an unavailable book', async () => {
    mockedGetBooks.mockResolvedValue(page([unavailableBook]));

    render(<Provider store={store}><Toast /><CataloguePage onAddToCart={vi.fn()} /></Provider>);

    expect(await screen.findByText('Domain-Driven Design')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /add to cart/i })).not.toBeInTheDocument();
  });

  it('appends the next page when the reader scrolls to the end', async () => {
    const secondBook = { ...availableBook, id: 'second-book', title: 'The Pragmatic Programmer' };
    mockedGetBooks
      .mockResolvedValueOnce(page([availableBook], true))
      .mockResolvedValueOnce({ ...page([secondBook]), offset: 5 });
    render(<Provider store={store}><Toast /><CataloguePage /></Provider>);
    await screen.findByText('Clean Code');

    await scrollToEnd();

    expect(await screen.findByText('The Pragmatic Programmer')).toBeInTheDocument();
    expect(mockedGetBooks).toHaveBeenLastCalledWith(5, 5, '');
  });

  it('reports a failure to load the next page', async () => {
    mockedGetBooks
      .mockResolvedValueOnce(page([availableBook], true))
      .mockRejectedValueOnce(new Error('network failure'));
    render(<Provider store={store}><Toast /><CataloguePage /></Provider>);
    await screen.findByText('Clean Code');

    await scrollToEnd();

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not load more books.');
  });

  it('de-duplicates books that reappear in a later page', async () => {
    mockedGetBooks.mockResolvedValue(page([availableBook, availableBook]));

    render(<Provider store={store}><Toast /><CataloguePage /></Provider>);

    expect(await screen.findAllByRole('heading', { name: 'Clean Code' })).toHaveLength(1);
  });
});
