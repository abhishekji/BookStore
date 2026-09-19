import type { Book } from '../../domain/types/types';
const baseUrl = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1';
export async function getBooks(): Promise<Book[]> {
  const response = await fetch(`${baseUrl}/books`);
  if (!response.ok) throw new Error('Unable to load books');
  return response.json() as Promise<Book[]>;
}
