import { describe, expect, it } from 'vitest';
import {
  cartFailed, cartLoaded, cartLoading, clearCart, clearCatalogue, hideToast,
  markBookSelected, setSelectedBookIds, showToast, store,
} from './store';
import type { Cart } from '../api/client/client';

const cart: Cart = {
  id: 'cart-1',
  items: [{ bookId: 'book-1', title: 'Clean Code', quantity: 2, unitPrice: 39.99, lineTotal: 79.98 }],
  total: 79.98,
};

const reset = () => {
  store.dispatch(clearCatalogue());
  store.dispatch(clearCart());
  store.dispatch(hideToast());
};

describe('catalogue state', () => {
  it('remembers a book that was added to the cart', () => {
    reset();
    store.dispatch(markBookSelected('book-1'));
    expect(store.getState().catalogue.selectedBookIds).toEqual(['book-1']);
  });

  it('does not record the same book twice', () => {
    reset();
    store.dispatch(markBookSelected('book-1'));
    store.dispatch(markBookSelected('book-1'));
    expect(store.getState().catalogue.selectedBookIds).toEqual(['book-1']);
  });

  it('replaces the selection with a de-duplicated list', () => {
    reset();
    store.dispatch(setSelectedBookIds(['book-1', 'book-2', 'book-1']));
    expect(store.getState().catalogue.selectedBookIds).toEqual(['book-1', 'book-2']);
  });

  it('forgets every selection when the catalogue is cleared', () => {
    store.dispatch(setSelectedBookIds(['book-1']));
    store.dispatch(clearCatalogue());
    expect(store.getState().catalogue.selectedBookIds).toEqual([]);
  });
});

describe('cart state', () => {
  it('clears a previous error when a new load starts', () => {
    reset();
    store.dispatch(cartFailed('Unable to load your cart.'));
    store.dispatch(cartLoading(true));
    expect(store.getState().cart).toMatchObject({ isLoading: true, error: undefined });
  });

  it('keeps an error visible when loading is switched off without a new request', () => {
    reset();
    store.dispatch(cartFailed('Unable to load your cart.'));
    store.dispatch(cartLoading(false));
    expect(store.getState().cart.error).toBe('Unable to load your cart.');
  });

  it('stores the loaded cart and ends the loading state', () => {
    reset();
    store.dispatch(cartLoading(true));
    store.dispatch(cartLoaded(cart));
    expect(store.getState().cart).toEqual({ cart, error: undefined, isLoading: false });
  });

  it('records a failure and ends the loading state', () => {
    reset();
    store.dispatch(cartLoading(true));
    store.dispatch(cartFailed('Unable to load your cart.'));
    expect(store.getState().cart).toMatchObject({ isLoading: false, error: 'Unable to load your cart.' });
  });

  it('drops the cart on logout', () => {
    store.dispatch(cartLoaded(cart));
    store.dispatch(clearCart());
    expect(store.getState().cart).toEqual({ cart: undefined, error: undefined, isLoading: false });
  });
});

describe('toast state', () => {
  it('records the message and its severity', () => {
    reset();
    store.dispatch(showToast({ type: 'success', message: 'Added to your cart.' }));
    expect(store.getState().toast).toEqual({ type: 'success', message: 'Added to your cart.' });
  });

  it('clears the message but keeps the last severity', () => {
    store.dispatch(showToast({ type: 'error', message: 'Could not load the catalogue.' }));
    store.dispatch(hideToast());
    expect(store.getState().toast).toEqual({ type: 'error', message: undefined });
  });
});
