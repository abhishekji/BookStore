import { StrictMode, useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Provider } from 'react-redux';
import { AuthProvider, useAuth } from './application/auth-state/AuthState';
import { CataloguePage } from './pages/catalogue/CataloguePage';
import { AuthPage } from './pages/auth/AuthPage';
import { CartPage } from './pages/cart/CartPage';
import { Toast } from './components/toast/Toast';
import { cartApi, type Cart } from './api/client/client';
import { cartFailed, cartLoaded, cartLoading, clearCart, store } from './state/store';
import './styles.css';

function App() {
  const { isAuthenticated, logout } = useAuth();
  const [authMode, setAuthMode] = useState<'login' | 'register'>(
    () => window.location.pathname === '/register' ? 'register' : 'login',
  );
  const [page, setPage] = useState<'catalogue' | 'cart'>(
    () => window.location.pathname === '/cart' ? 'cart' : 'catalogue',
  );
  const [cart, setCart] = useState<Cart>();

  const navigate = (nextPage: 'catalogue' | 'cart') => {
    setPage(nextPage);
    const path = nextPage === 'cart' ? '/cart' : '/books';
    if (window.location.pathname !== path) window.history.pushState({}, '', path);
  };

  const navigateAuth = (mode: 'login' | 'register') => {
    setAuthMode(mode);
    const path = mode === 'register' ? '/register' : '/login';
    if (window.location.pathname !== path) window.history.pushState({}, '', path);
  };

  useEffect(() => {
    const handleBrowserNavigation = () => {
      if (!isAuthenticated) {
        setAuthMode(window.location.pathname === '/register' ? 'register' : 'login');
      } else {
        setPage(window.location.pathname === '/cart' ? 'cart' : 'catalogue');
      }
    };
    window.addEventListener('popstate', handleBrowserNavigation);
    return () => window.removeEventListener('popstate', handleBrowserNavigation);
  }, [isAuthenticated]);

  useEffect(() => {
    if (isAuthenticated) {
      store.dispatch(cartLoading(true));
      cartApi.getCart().then(nextCart => {
        setCart(nextCart);
        store.dispatch(cartLoaded(nextCart));
      }).catch(error => {
        setCart(undefined);
        store.dispatch(cartFailed(error instanceof Error ? error.message : 'Unable to load your cart.'));
      });
    } else {
      setCart(undefined);
      store.dispatch(clearCart());
      navigateAuth('login');
    }
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return <>
      <Toast />
      <AuthPage mode={authMode} onLoginSuccess={() => navigate('catalogue')} onModeChange={navigateAuth} />
    </>;
  }

  const addToCart = async (bookId: string) => {
    const updatedCart = await cartApi.addToCart(bookId);
    setCart(updatedCart);
    store.dispatch(cartLoaded(updatedCart));
  };

  return <>
    <Toast />
    <header className="site-header">
      <button className="brand" type="button" onClick={() => navigate('catalogue')}>BOOK<span>HUB</span></button>
      <nav className="site-nav" aria-label="Main navigation">
        <button className={page === 'catalogue' ? 'active' : ''} type="button" onClick={() => navigate('catalogue')}>Books</button>
        <button className={`cart-button ${page === 'cart' ? 'active' : ''}`} type="button" onClick={() => navigate('cart')}>
          Cart <span className="cart-count">{cart?.items.reduce((total, item) => total + item.quantity, 0) ?? 0}</span>
        </button>
        <button type="button" onClick={logout}>Log out</button>
      </nav>
    </header>
    {page === 'catalogue' && <CataloguePage onAddToCart={addToCart} onGoToCart={() => navigate('cart')} />}
    {page === 'cart' && <CartPage onCartChange={setCart} onContinueShopping={() => navigate('catalogue')} />}
  </>;
}

createRoot(document.getElementById('root')!).render(
  <StrictMode><Provider store={store}><AuthProvider><App /></AuthProvider></Provider></StrictMode>,
);
