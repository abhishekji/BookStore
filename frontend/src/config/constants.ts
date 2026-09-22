export const API = {
  defaultUrl: 'http://localhost:8080/api/v1',
  booksResource: '/books',
  ordersResource: '/orders',
} as const;

export const UI = {
  bookPriceDecimalPlaces: 2,
  toastAutoDismissMs: 4_000,
} as const;
