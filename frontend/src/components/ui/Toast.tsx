import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react"
import type { ReactNode } from "react"
import {
  AlertTriangle,
  CheckCircle2,
  Info,
  X,
  XCircle,
} from "lucide-react"

type ToastType = "success" | "error" | "warning" | "info"

interface Toast {
  id: string
  type: ToastType
  message: string
}

interface ToastContextValue {
  toast: (type: ToastType, message: string) => void
}

const ToastContext = createContext<ToastContextValue | null>(null)

const toastMeta = {
  success: {
    icon: CheckCircle2,
    label: "Done",
  },
  error: {
    icon: XCircle,
    label: "Something went wrong",
  },
  warning: {
    icon: AlertTriangle,
    label: "A quick heads-up",
  },
  info: {
    icon: Info,
    label: "Good to know",
  },
} satisfies Record<
  ToastType,
  {
    icon: typeof CheckCircle2
    label: string
  }
>

export function useToast() {
  const context = useContext(ToastContext)

  if (!context) {
    throw new Error("useToast must be used within ToastProvider")
  }

  return context
}

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])
  const timers = useRef(new Map<string, ReturnType<typeof setTimeout>>())

  useEffect(() => {
    const currentTimers = timers.current

    return () => {
      currentTimers.forEach(clearTimeout)
      currentTimers.clear()
    }
  }, [])

  const remove = useCallback((id: string) => {
    const timer = timers.current.get(id)

    if (timer) {
      clearTimeout(timer)
      timers.current.delete(id)
    }

    setToasts((current) => current.filter((item) => item.id !== id))
  }, [])

  const toast = useCallback((type: ToastType, message: string) => {
    const id = crypto.randomUUID()

    setToasts((current) =>
      [
        ...current.filter((item) => item.message !== message),
        { id, type, message },
      ].slice(-4),
    )

    const timer = setTimeout(() => {
      setToasts((current) => current.filter((item) => item.id !== id))
      timers.current.delete(id)
    }, 5000)

    timers.current.set(id, timer)
  }, [])

  return (
    <ToastContext.Provider value={{ toast }}>
      {children}

      <div
        className="toast-viewport"
        aria-live="polite"
        aria-atomic="false"
      >
        {toasts.map(({ id, type, message }) => {
          const { icon: Icon, label } = toastMeta[type]

          return (
            <div
              key={id}
              className={`sp-toast sp-toast--${type}`}
              role={type === "error" ? "alert" : "status"}
            >
              <div className="sp-toast__icon" aria-hidden="true">
                <Icon size={18} />
              </div>

              <div className="sp-toast__content">
                <strong>{label}</strong>
                <p>{message}</p>
              </div>

              <button
                type="button"
                className="sp-toast__close"
                onClick={() => remove(id)}
                aria-label="Dismiss notification"
              >
                <X size={15} aria-hidden="true" />
              </button>
            </div>
          )
        })}
      </div>
    </ToastContext.Provider>
  )
}