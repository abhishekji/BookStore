import type { Book } from '../../domain/types/types';
const DEFAULT_API_URL = 'http://localhost:8080/api/v1';
const BOOKS_RESOURCE = '/books';
const baseUrl = import.meta.env.VITE_API_URL ?? DEFAULT_API_URL;
async function getBooks(): Promise<Book[]> {
  const response = await fetch(`${baseUrl}${BOOKS_RESOURCE}`);
  if (!response.ok) throw new Error('Unable to load books');
  return response.json() as Promise<Book[]>;
}
export const bookApi = { getBooks };
export { getBooks };
