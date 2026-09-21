import { useEffect, useRef, useState } from 'react';
import { cartApi, orderApi, type Cart, type Order } from '../../api/client/client';
import { useAppDispatch } from '../../state/hooks';
import { cartLoaded, clearCart, showToast } from '../../state/store';

function newCheckoutKey() {
  return crypto.randomUUID();
}

export function CheckoutPage({ onConfirmed, onBack }: { onConfirmed: (order: Order) => void; onBack: () => void }) {
  const dispatch = useAppDispatch();
  const [cart, setCart] = useState<Cart>();
  const [error, setError] = useState('');
  const [placing, setPlacing] = useState(false);
  // One key belongs to one user checkout attempt; it survives network retries in this mounted page.
  const key = useRef(newCheckoutKey());
  const loadCart = () => {
    setError('');
    return cartApi.getCart().then(setCart)
      .catch(e => setError(e instanceof Error ? e.message : 'Unable to load checkout.'));
  };
  useEffect(() => { void loadCart(); }, []);
  const placeOrder = async () => {
    setPlacing(true); setError('');
    try {
      const order = await orderApi.checkout(key.current);
      dispatch(clearCart());
      dispatch(showToast({ type: 'success', message: 'Your order has been placed.' }));
      onConfirmed(order);
    } catch (e) {
      const message = e instanceof Error ? e.message : 'Unable to place order.';
      setError(message);
      dispatch(showToast({ type: 'error', message }));
    } finally { setPlacing(false); }
  };
  if (!cart) return <main className="cart-page"><p role="status">Loading checkout...</p>{error && <><p role="alert">{error}</p><button className="secondary-button" type="button" onClick={loadCart}>Retry loading checkout</button></>}</main>;
  return <main className="cart-page"><div className="page-heading"><div><p className="section-kicker">Checkout</p><h1>Review your order</h1></div><button className="secondary-button" type="button" onClick={onBack}>Back to cart</button></div>
    {cart.items.map(item => <article key={item.bookId}><div><h2>{item.title}</h2><p>{item.quantity} × ${item.unitPrice.toFixed(2)}</p></div><strong>${item.lineTotal.toFixed(2)}</strong></article>)}
    <aside className="cart-summary"><span>Subtotal</span><strong>${cart.total.toFixed(2)}</strong><span>Total</span><strong>${cart.total.toFixed(2)}</strong><button className="primary-button" type="button" disabled={placing} onClick={placeOrder}>{placing ? 'Placing order…' : 'Place Order'}</button>{error && <p role="alert">{error}</p>}</aside>
  </main>;
}
