import { createContext, useContext, useState, type ReactNode } from 'react';
import { authApi, TOKEN_KEY, type AuthResponse } from '../../api/client/client';
type AuthState = {
  isAuthenticated: boolean;
  user?: AuthResponse;
  setAuthenticated: (value: boolean) => void;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, displayName: string) => Promise<AuthResponse>;
  logout: () => void;
};
const Context = createContext<AuthState | undefined>(undefined);
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthResponse>();
  const [isAuthenticated, setIsAuthenticated] = useState(
    () => Boolean(localStorage.getItem(TOKEN_KEY)),
  );
  const setAuthenticated = (value: boolean) => {
    setIsAuthenticated(value);
    if (!value) {
      localStorage.removeItem(TOKEN_KEY);
      setUser(undefined);
    }
  };
  const login = async (email: string, password: string) => {
    const response = await authApi.login(email, password);
    localStorage.setItem(TOKEN_KEY, response.token);
    setUser(response);
    setIsAuthenticated(true);
  };
  const register = async (email: string, password: string, displayName: string) => {
    return authApi.register(email, password, displayName);
  };
  return <Context.Provider value={{
    isAuthenticated, user, setAuthenticated, login, register,
    logout: () => setAuthenticated(false),
  }}>{children}</Context.Provider>;
}
export function useAuth() {
  const value = useContext(Context);
  if (!value) throw new Error('useAuth must be used within AuthProvider');
  return value;
}
