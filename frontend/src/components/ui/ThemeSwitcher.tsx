import { Sun, Moon, Monitor } from 'lucide-react';
import { useTheme, type Theme } from '@/app/providers/ThemeProvider';

const options: { value: Theme; label: string; Icon: React.FC<React.SVGProps<SVGSVGElement>> }[] = [
  { value: 'light', label: 'Light', Icon: Sun },
  { value: 'dark', label: 'Dark', Icon: Moon },
  { value: 'system', label: 'System', Icon: Monitor },
];

export default function ThemeSwitcher({ compact = false }: { compact?: boolean }) {
  const { theme, setTheme } = useTheme();

  if (compact) {
    const current = options.find((o) => o.value === theme) ?? options[2];
    const next = options[(options.indexOf(current) + 1) % options.length];
    return (
      <button
        onClick={() => setTheme(next.value)}
        aria-label={`Switch to ${next.label} theme`}
        className="p-2 rounded-lg transition-colors"
        style={{ color: 'var(--fg-muted)' }}
        onMouseEnter={(e) => (e.currentTarget.style.background = 'var(--bg-subtle)')}
        onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
      >
        <current.Icon style={{ width: 18, height: 18 }} />
      </button>
    );
  }

  return (
    <div className="flex items-center gap-1 p-1 rounded-lg" style={{ background: 'var(--bg-subtle)', border: '1px solid var(--border)' }}>
      {options.map(({ value, label, Icon }) => (
        <button
          key={value}
          onClick={() => setTheme(value)}
          aria-label={`${label} theme`}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium transition-all"
          style={{
            background: theme === value ? 'var(--surface)' : 'transparent',
            color: theme === value ? 'var(--fg)' : 'var(--fg-muted)',
            boxShadow: theme === value ? 'var(--shadow-sm)' : 'none',
          }}
        >
          <Icon style={{ width: 13, height: 13 }} />
          {label}
        </button>
      ))}
    </div>
  );
}
