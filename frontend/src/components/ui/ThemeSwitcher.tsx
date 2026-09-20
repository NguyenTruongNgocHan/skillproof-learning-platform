import { Sun, Moon } from 'lucide-react';
import { useTheme } from '@/app/providers/ThemeProvider';

export default function ThemeSwitcher({ compact: _compact = false }: { compact?: boolean }) {
  const { resolvedTheme, setTheme } = useTheme();
  const isDark = resolvedTheme === 'dark';
  const Icon = isDark ? Sun : Moon;
  return (
    <button
      type="button"
      onClick={() => setTheme(isDark ? 'light' : 'dark')}
      aria-label={isDark ? 'Switch to light theme' : 'Switch to dark theme'}
      title={isDark ? 'Light mode' : 'Dark mode'}
      className="theme-toggle"
    >
      <Icon aria-hidden="true" size={18} />
    </button>
  );
}
