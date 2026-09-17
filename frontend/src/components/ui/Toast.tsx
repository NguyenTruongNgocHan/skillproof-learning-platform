import { createContext, useContext, useState, ReactNode, useCallback } from 'react';
import { CheckCircle, XCircle, AlertCircle, X } from 'lucide-react';

type ToastType = 'success' | 'error' | 'warning' | 'info';

interface Toast {
  id: string;
  type: ToastType;
  message: string;
}

interface ToastContextValue {
  toast: (type: ToastType, message: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
}

const icons = {
  success: CheckCircle,
  error: XCircle,
  warning: AlertCircle,
  info: AlertCircle,
};

const colors: Record<ToastType, string> = {
  success: 'var(--success)',
  error: 'var(--error)',
  warning: 'var(--warning)',
  info: 'var(--brand)',
};

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const toast = useCallback((type: ToastType, message: string) => {
    const id = crypto.randomUUID();
    setToasts((prev) => [
      ...prev.filter((item) => item.message !== message),
      { id, type, message },
    ].slice(-4));
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 4000);
  }, []);

  const remove = (id: string) => setToasts((prev) => prev.filter((t) => t.id !== id));

  return (
    <ToastContext.Provider value={{ toast }}>
      {children}
      <div className="fixed top-4 right-4 z-50 flex w-[calc(100%-2rem)] max-w-sm flex-col gap-2 pointer-events-none">
        {toasts.map(({ id, type, message }) => {
          const Icon = icons[type];
          return (
            <div
              key={id}
              className="flex items-start gap-3 px-4 py-3 rounded-xl pointer-events-auto"
              style={{ background: 'var(--surface)', boxShadow: 'var(--shadow-lg)', border: '1px solid var(--border)' }}
            >
              <Icon style={{ width: 16, height: 16, color: colors[type], flexShrink: 0, marginTop: 2 }} />
              <p className="text-sm flex-1" style={{ color: 'var(--fg)' }}>{message}</p>
              <button onClick={() => remove(id)} aria-label="Dismiss" style={{ color: 'var(--fg-muted)' }}>
                <X style={{ width: 14, height: 14 }} />
              </button>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
}
