import { act, fireEvent, render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { Toast } from './Toast';
import { hideToast, showToast, store } from '../../state/store';
import { UI } from '../../config/constants';

const renderToast = () => render(<Provider store={store}><Toast /></Provider>);

describe('Toast', () => {
  afterEach(() => {
    act(() => { store.dispatch(hideToast()); });
    vi.useRealTimers();
  });

  it('renders nothing while there is no notification', () => {
    const { container } = renderToast();
    expect(container).toBeEmptyDOMElement();
  });

  it('announces errors assertively', () => {
    renderToast();
    act(() => { store.dispatch(showToast({ type: 'error', message: 'Could not load the catalogue.' })); });
    const alert = screen.getByRole('alert');
    expect(alert).toHaveTextContent('Could not load the catalogue.');
    expect(alert).toHaveClass('toast--error');
  });

  it('announces successes politely', () => {
    renderToast();
    act(() => { store.dispatch(showToast({ type: 'success', message: 'Added to your cart.' })); });
    expect(screen.getByRole('status')).toHaveTextContent('Added to your cart.');
  });

  it('lets the reader dismiss the notification', () => {
    renderToast();
    act(() => { store.dispatch(showToast({ type: 'error', message: 'Something went wrong.' })); });
    fireEvent.click(screen.getByRole('button', { name: /dismiss notification/i }));
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('dismisses itself after the display window elapses', () => {
    vi.useFakeTimers();
    renderToast();
    act(() => { store.dispatch(showToast({ type: 'success', message: 'Saved.' })); });
    expect(screen.getByRole('status')).toBeInTheDocument();
    act(() => { vi.advanceTimersByTime(UI.toastAutoDismissMs); });
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
  });
});
