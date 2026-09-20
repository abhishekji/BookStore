import { configureStore, createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { Cart } from '../api/client/client';

const catalogueSlice = createSlice({
  name: 'catalogue',
  initialState: { selectedBookIds: [] as string[] },
  reducers: {
    markBookSelected(state, action: PayloadAction<string>) {
      if (!state.selectedBookIds.includes(action.payload)) state.selectedBookIds.push(action.payload);
    },
    setSelectedBookIds(state, action: PayloadAction<string[]>) {
      state.selectedBookIds = [...new Set(action.payload)];
    },
    clearCatalogue(state) {
      state.selectedBookIds = [];
    },
  },
});

const cartSlice = createSlice({
  name: 'cart',
  initialState: { cart: undefined as Cart | undefined, error: undefined as string | undefined, isLoading: false },
  reducers: {
    cartLoading(state, action: PayloadAction<boolean>) {
      state.isLoading = action.payload;
      if (action.payload) state.error = undefined;
    },
    cartLoaded(state, action: PayloadAction<Cart>) {
      state.cart = action.payload;
      state.isLoading = false;
      state.error = undefined;
    },
    cartFailed(state, action: PayloadAction<string>) {
      state.isLoading = false;
      state.error = action.payload;
    },
    clearCart(state) {
      state.cart = undefined;
      state.error = undefined;
      state.isLoading = false;
    },
  },
});

const toastSlice = createSlice({
  name: 'toast',
  initialState: { message: undefined as string | undefined, type: 'error' as 'error' | 'success' },
  reducers: {
    showToast(state, action: PayloadAction<{ message: string; type: 'error' | 'success' }>) {
      state.message = action.payload.message;
      state.type = action.payload.type;
    },
    hideToast(state) {
      state.message = undefined;
    },
  },
});

export const { markBookSelected, setSelectedBookIds, clearCatalogue } = catalogueSlice.actions;
export const { cartLoading, cartLoaded, cartFailed, clearCart } = cartSlice.actions;
export const { showToast, hideToast } = toastSlice.actions;
export const store = configureStore({
  reducer: { catalogue: catalogueSlice.reducer, cart: cartSlice.reducer, toast: toastSlice.reducer },
});
export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
