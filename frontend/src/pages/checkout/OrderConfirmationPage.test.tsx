import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { OrderConfirmationPage } from './OrderConfirmationPage';
import type { Order } from '../../api/client/client';

const order: Order = {
  id: 'order-42',
  status: 'CONFIRMED',
  createdAt: '2026-09-21T10:15:00Z',
  total: 89.98,
  items: [{ bookId: 'book-1', bookTitle: 'Clean Code', quantity: 2, unitPrice: 44.99, lineTotal: 89.98 }],
};

describe('OrderConfirmationPage', () => {
  it('confirms the order and shows its identifier', () => {
    render(<OrderConfirmationPage order={order} onContinue={() => {}} />);
    expect(screen.getByRole('heading', { name: /thank you for your order/i })).toBeInTheDocument();
    expect(screen.getByText('order-42')).toBeInTheDocument();
  });

  it('formats the total as currency with two decimals', () => {
    render(<OrderConfirmationPage order={{ ...order, total: 5 }} onContinue={() => {}} />);
    expect(screen.getByText('Total: $5.00')).toBeInTheDocument();
  });

  it('returns the reader to the catalogue', async () => {
    const onContinue = vi.fn();
    render(<OrderConfirmationPage order={order} onContinue={onContinue} />);
    await userEvent.click(screen.getByRole('button', { name: /continue shopping/i }));
    expect(onContinue).toHaveBeenCalledTimes(1);
  });
});
