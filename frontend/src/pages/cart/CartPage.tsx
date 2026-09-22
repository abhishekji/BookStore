import { useEffect, useState } from 'react';
import { cartApi, type Cart } from '../../api/client/client';
import { useAuth } from '../../application/auth-state/AuthState';
import './CartPage.css';
import { useAppDispatch } from '../../state/hooks';
import { cartFailed, cartLoaded, cartLoading, showToast } from '../../state/store';

export function CartPage({ onCartChange, onContinueShopping, onCheckout }: {
  onCartChange?: (cart: Cart) => void;
  onContinueShopping?: () => void;
  onCheckout?: () => void;
}) {
  const { isAuthenticated } = useAuth();
  const dispatch = useAppDispatch();
  const [cart, setCart] = useState<Cart>();
  const [error, setError] = useState('');
  const [updatingBookId, setUpdatingBookId] = useState<string>();
  const updateCart = (nextCart: Cart) => {
    setCart(nextCart);
    dispatch(cartLoaded(nextCart));
    onCartChange?.(nextCart);
  };
  const load = () => {
    setError('');
    dispatch(cartLoading(true));
    return cartApi.getCart().then(updateCart).catch(error => {
      const message = error instanceof Error ? error.message : 'Unable to load your cart.';
      setError(message);
      dispatch(showToast({ type: 'error', message }));
      dispatch(cartFailed(message));
    });
  };
  const changeQuantity = async (bookId: string, quantity: number) => {
    setError('');
    setUpdatingBookId(bookId);
    try {
      updateCart(await cartApi.changeCartQuantity(bookId, quantity));
    } catch (updateError) {
      const message = updateError instanceof Error ? updateError.message : 'Unable to update quantity.';
      setError(message);
      dispatch(showToast({ type: 'error', message }));
    } finally {
      setUpdatingBookId(undefined);
    }
  };
  const removeItem = async (bookId: string) => {
    try {
      await cartApi.removeFromCart(bookId);
      await load();
    } catch (removeError) {
      const message = removeError instanceof Error ? removeError.message : 'Unable to remove item.';
      setError(message);
      dispatch(showToast({ type: 'error', message }));
    }
  };
  useEffect(() => {
    if (isAuthenticated) load();
    else dispatch(showToast({ type: 'error', message: 'Please log in to access your cart.' }));
  }, [isAuthenticated]);
  if (!isAuthenticated) return null;
  if (error) return <main className="cart-page"><button className="secondary-button" type="button" onClick={load}>Retry loading cart</button></main>;
  if (!cart) return <p role="status">Loading cart...</p>;
  if (cart.items.length === 0) return <main className="cart-page cart-page--empty"><div className="empty-cart"><h1>Your cart is empty</h1><p>Add a book from the catalogue to get started.</p><button className="secondary-button" type="button" onClick={onContinueShopping}>Continue shopping</button></div></main>;
  return <main className="cart-page"><div className="page-heading"><div><p className="section-kicker">Shopping cart</p><h1>Your cart</h1></div><button className="secondary-button" type="button" onClick={onContinueShopping}>Continue shopping</button></div>
    {cart.items.map(item => <article key={item.bookId}>
      <div><h2>{item.title}</h2><p className="cart-item__price">${item.unitPrice.toFixed(2)} each</p></div>
      <p className="cart-item__line-total">${item.lineTotal.toFixed(2)}</p>
      <div className="quantity-control"><button type="button" aria-label={`Decrease ${item.title} quantity`}
        onClick={() => changeQuantity(item.bookId, item.quantity - 1)} disabled={updatingBookId === item.bookId || item.quantity <= 1}> - </button>
      <span aria-label={`${item.title} quantity`}>{item.quantity}</span>
      <button type="button" aria-label={`Increase ${item.title} quantity`}
        onClick={() => changeQuantity(item.bookId, item.quantity + 1)} disabled={updatingBookId === item.bookId}>+</button></div>
      <button className="remove-button" type="button" onClick={() => removeItem(item.bookId)}>Remove</button>
    </article>)}
    <aside className="cart-summary"><span>Subtotal</span><strong>${cart.total.toFixed(2)}</strong><button className="primary-button" type="button" onClick={onCheckout}>Proceed to Checkout</button></aside>
  </main>;
}
