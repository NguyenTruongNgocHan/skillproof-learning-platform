import {
  createContext,
  useContext,
  useEffect,
  useLayoutEffect,
  useState,
} from "react"
import type { ReactNode } from "react"
export type Theme = "light" | "dark" | "system"
const STORAGE_KEY = "skillproof-theme"
interface ThemeContextValue {
  theme: Theme
  resolvedTheme: "light" | "dark"
  setTheme: (theme: Theme) => void
}
const ThemeContext = createContext<ThemeContextValue | null>(null)
export function useTheme() {
  const value = useContext(ThemeContext)
  if (!value) throw new Error("useTheme must be used within ThemeProvider")
  return value
}
function systemTheme(): "light" | "dark" {
  return window.matchMedia("(prefers-color-scheme: dark)").matches
    ? "dark"
    : "light"
}
export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setPreference] = useState<Theme>(() => {
    try {
      const value = localStorage.getItem(STORAGE_KEY)
      return value === "light" || value === "dark" || value === "system"
        ? value
        : "system"
    } catch {
      return "system"
    }
  })
  const [system, setSystem] = useState(systemTheme)
  const resolvedTheme = theme === "system" ? system : theme
  useEffect(() => {
    const media = window.matchMedia("(prefers-color-scheme: dark)")
    const onChange = () => setSystem(media.matches ? "dark" : "light")
    media.addEventListener("change", onChange)
    return () => media.removeEventListener("change", onChange)
  }, [])
  useLayoutEffect(() => {
    document.documentElement.dataset.theme = resolvedTheme
    document.documentElement.style.colorScheme = resolvedTheme
  }, [resolvedTheme])
  function setTheme(value: Theme) {
    try {
      localStorage.setItem(STORAGE_KEY, value)
    } catch {
      /* Theme remains usable without storage. */
    }
    setPreference(value)
  }
  return (
    <ThemeContext.Provider value={{ theme, resolvedTheme, setTheme }}>
      {children}
    </ThemeContext.Provider>
  )
}
