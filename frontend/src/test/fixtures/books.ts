import type { Book } from '../../domain/types/types';

export const availableBook: Book = {
  id: 'available-book',
  title: 'Clean Code',
  author: 'Robert Martin',
  isbn: '9780132350884',
  price: 39.99,
  stockQuantity: 3,
  inStock: true,
};

export const unavailableBook: Book = {
  id: 'unavailable-book',
  title: 'Domain-Driven Design',
  author: 'Eric Evans',
  isbn: '9780321125217',
  price: 59.99,
  stockQuantity: 0,
  inStock: false,
};
