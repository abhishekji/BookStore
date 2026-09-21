import type { Order } from '../../api/client/client';

export function OrderConfirmationPage({ order, onContinue }: { order: Order; onContinue: () => void }) {
  return <main className="cart-page cart-page--empty"><div className="empty-cart"><p className="section-kicker">Order confirmed</p><h1>Thank you for your order</h1><p>Order ID: <code>{order.id}</code></p><p>Total: ${order.total.toFixed(2)}</p><button className="primary-button" type="button" onClick={onContinue}>Continue shopping</button></div></main>;
}
