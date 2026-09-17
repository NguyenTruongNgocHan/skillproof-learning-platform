import { CheckCircle, Circle } from 'lucide-react';
import type { PasswordCheckResult, PasswordStrength } from '@/features/auth/validation/passwordPolicy';

const requirements = [
  { key: 'minLength' as const, label: 'At least 12 characters' },
  { key: 'uppercase' as const, label: 'An uppercase letter (A-Z)' },
  { key: 'lowercase' as const, label: 'A lowercase letter (a-z)' },
  { key: 'number' as const, label: 'A number (0-9)' },
  { key: 'special' as const, label: 'A special character (!@#$...)' },
  { key: 'noLeadingTrailingSpace' as const, label: 'No leading or trailing spaces' },
  { key: 'notCommon' as const, label: 'Not a commonly used password' },
];

const strengthColors: Record<PasswordStrength, string> = {
  'weak': '#DC2626',
  'fair': '#D97706',
  'strong': '#16A34A',
  'very-strong': '#059669',
};

const strengthLabels: Record<PasswordStrength, string> = {
  'weak': 'Weak',
  'fair': 'Fair',
  'strong': 'Strong',
  'very-strong': 'Very Strong',
};

const strengthWidth: Record<PasswordStrength, string> = {
  'weak': '25%',
  'fair': '50%',
  'strong': '75%',
  'very-strong': '100%',
};

interface PasswordRequirementsProps {
  checks: PasswordCheckResult;
  strength: PasswordStrength;
  password: string;
}

export default function PasswordRequirements({ checks, strength, password }: PasswordRequirementsProps) {
  if (!password) return null;
  return (
    <div className="rounded-lg p-3 flex flex-col gap-2.5" style={{ background: 'var(--bg-subtle)', border: '1px solid var(--border)' }}>
      <div className="flex flex-col gap-1">
        <div className="flex justify-between text-xs mb-0.5" style={{ color: 'var(--fg-muted)' }}>
          <span>Password strength</span>
          <span style={{ color: strengthColors[strength], fontWeight: 600 }}>{strengthLabels[strength]}</span>
        </div>
        <div className="h-1.5 rounded-full overflow-hidden" style={{ background: 'var(--border)' }}>
          <div
            className="h-full rounded-full transition-all duration-300"
            style={{ width: strengthWidth[strength], background: strengthColors[strength] }}
          />
        </div>
      </div>
      <div className="flex flex-col gap-1">
        {requirements.map(({ key, label }) => (
          <div key={key} className="flex items-center gap-2">
            {checks[key]
              ? <CheckCircle style={{ width: 13, height: 13, color: 'var(--success)', flexShrink: 0 }} />
              : <Circle style={{ width: 13, height: 13, color: 'var(--fg-muted)', flexShrink: 0 }} />
            }
            <span className="text-xs" style={{ color: checks[key] ? 'var(--fg)' : 'var(--fg-muted)' }}>{label}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
