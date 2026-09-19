import { createContext, useContext, useState, type ReactNode } from 'react';
type AuthState = { isAuthenticated: boolean; setAuthenticated: (value: boolean) => void };
const Context = createContext<AuthState | undefined>(undefined);
export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setAuthenticated] = useState(false);
  return <Context.Provider value={{ isAuthenticated, setAuthenticated }}>{children}</Context.Provider>;
}
export function useAuth() {
  const value = useContext(Context);
  if (!value) throw new Error('useAuth must be used within AuthProvider');
  return value;
}
