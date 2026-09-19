import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { AuthProvider } from './application/auth-state/AuthState';
import { CataloguePage } from './pages/catalogue/CataloguePage';
import './styles.css';
createRoot(document.getElementById('root')!).render(<StrictMode><AuthProvider><CataloguePage /></AuthProvider></StrictMode>);
