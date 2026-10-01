import { Monitor, Moon, Sun } from "lucide-react"
import { useTheme } from "@/app/providers/ThemeProvider"
export default function ThemeSwitcher({
  compact: _compact = false,
}: {
  compact?: boolean
}) {
  const { theme, resolvedTheme, setTheme } = useTheme()
  const next =
    theme === "system" ? "light" : theme === "light" ? "dark" : "system"
  const Icon =
    theme === "system" ? Monitor : resolvedTheme === "dark" ? Moon : Sun
  return (
    <button
      type="button"
      className="theme-toggle"
      onClick={() => setTheme(next)}
      title={`Current: ${theme}. Switch to ${next}.`}
      aria-label={`Theme: ${theme}. Switch to ${next}.`}
    >
      <Icon aria-hidden="true" size={18} />
    </button>
  )
}
