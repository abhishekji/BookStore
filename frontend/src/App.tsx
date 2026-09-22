import { useEffect, useState } from 'react';
import { AuthProvider, useAuth } from './application/auth-state/AuthState';
import { CataloguePage } from './pages/catalogue/CataloguePage';
import { AuthPage } from './pages/auth/AuthPage';
import { CartPage } from './pages/cart/CartPage';
import { CheckoutPage } from './pages/checkout/CheckoutPage';
import { OrderConfirmationPage } from './pages/checkout/OrderConfirmationPage';
import { Toast } from './components/toast/Toast';
import { cartApi, type Cart, type Order } from './api/client/client';
import { cartFailed, cartLoaded, cartLoading, clearCart, store } from './state/store';

type Page = 'catalogue' | 'cart' | 'checkout' | 'confirmation';
const pageForPath = (path: string): Page => path === '/cart' ? 'cart' : path === '/checkout' ? 'checkout' : 'catalogue';
const pathForPage = (page: Page) => ({ catalogue: '/books', cart: '/cart', checkout: '/checkout', confirmation: '/order-confirmation' })[page];

function BookstoreApp() {
  const { isAuthenticated, logout } = useAuth();
  const [authMode, setAuthMode] = useState<'login' | 'register'>(() => window.location.pathname === '/register' ? 'register' : 'login');
  const [page, setPage] = useState<Page>(() => pageForPath(window.location.pathname));
  const [cart, setCart] = useState<Cart>();
  const [confirmedOrder, setConfirmedOrder] = useState<Order>();
  const navigate = (nextPage: Page) => {
    setPage(nextPage);
    const path = pathForPage(nextPage);
    if (window.location.pathname !== path) window.history.pushState({}, '', path);
  };
  const navigateAuth = (mode: 'login' | 'register') => {
    setAuthMode(mode);
    const path = mode === 'register' ? '/register' : '/login';
    if (window.location.pathname !== path) window.history.pushState({}, '', path);
  };
  useEffect(() => {
    const browserNavigation = () => isAuthenticated
      ? setPage(pageForPath(window.location.pathname))
      : setAuthMode(window.location.pathname === '/register' ? 'register' : 'login');
    window.addEventListener('popstate', browserNavigation);
    return () => window.removeEventListener('popstate', browserNavigation);
  }, [isAuthenticated]);
  useEffect(() => {
    if (!isAuthenticated) {
      setCart(undefined); store.dispatch(clearCart()); navigateAuth('login'); return;
    }
    store.dispatch(cartLoading(true));
    cartApi.getCart().then(nextCart => { setCart(nextCart); store.dispatch(cartLoaded(nextCart)); })
      .catch(error => { setCart(undefined); store.dispatch(cartFailed(error instanceof Error ? error.message : 'Unable to load your cart.')); });
  }, [isAuthenticated]);
  if (!isAuthenticated) return <><Toast /><AuthPage mode={authMode} onLoginSuccess={() => navigate('catalogue')} onModeChange={navigateAuth} /></>;
  const addToCart = async (bookId: string) => {
    const updatedCart = await cartApi.addToCart(bookId); setCart(updatedCart); store.dispatch(cartLoaded(updatedCart));
  };
  return <><Toast /><header className="site-header"><button className="brand" type="button" onClick={() => navigate('catalogue')}>BOOK<span>HUB</span></button><nav className="site-nav" aria-label="Main navigation">
    <button className={page === 'catalogue' ? 'active' : ''} type="button" onClick={() => navigate('catalogue')}>Books</button>
    <button className={`cart-button ${page === 'cart' ? 'active' : ''}`} type="button" onClick={() => navigate('cart')}>Cart <span className="cart-count">{cart?.items.reduce((total, item) => total + item.quantity, 0) ?? 0}</span></button><button type="button" onClick={logout}>Log out</button>
  </nav></header>
  {page === 'catalogue' && <CataloguePage onAddToCart={addToCart} onGoToCart={() => navigate('cart')} />}
  {page === 'cart' && <CartPage onCartChange={setCart} onContinueShopping={() => navigate('catalogue')} onCheckout={() => navigate('checkout')} />}
  {page === 'checkout' && <CheckoutPage onBack={() => navigate('cart')} onConfirmed={order => { setConfirmedOrder(order); setCart({ id: cart?.id ?? '', items: [], total: 0 }); navigate('confirmation'); }} />}
  {page === 'confirmation' && confirmedOrder && <OrderConfirmationPage order={confirmedOrder} onContinue={() => navigate('catalogue')} />}</>;
}

export function App() { return <AuthProvider><BookstoreApp /></AuthProvider>; }
