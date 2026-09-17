import { useState, InputHTMLAttributes, forwardRef } from 'react';
import { Eye, EyeOff } from 'lucide-react';

interface PasswordInputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label?: string;
  error?: string;
  hint?: string;
}

const PasswordInput = forwardRef<HTMLInputElement, PasswordInputProps>(
  ({ label, error, hint, className = '', id, ...props }, ref) => {
    const [visible, setVisible] = useState(false);
    const inputId = id ?? 'password-field';
    return (
      <div className="flex flex-col gap-1.5">
        {label && (
          <label htmlFor={inputId} className="text-sm font-medium" style={{ color: 'var(--fg)' }}>
            {label}
          </label>
        )}
        <div className="relative">
          <input
            ref={ref}
            id={inputId}
            type={visible ? 'text' : 'password'}
            className={`w-full px-3.5 py-2.5 pr-10 rounded-lg text-sm outline-none transition-all ${className}`}
            style={{
              background: 'var(--surface)',
              color: 'var(--fg)',
              border: `1px solid ${error ? 'var(--error)' : 'var(--border)'}`,
            }}
            onFocus={(e) => {
              e.currentTarget.style.borderColor = error ? 'var(--error)' : 'var(--brand)';
              e.currentTarget.style.boxShadow = `0 0 0 3px ${error ? 'rgba(220,38,38,0.1)' : 'rgba(255,79,139,0.1)'}`;
            }}
            onBlur={(e) => {
              e.currentTarget.style.borderColor = error ? 'var(--error)' : 'var(--border)';
              e.currentTarget.style.boxShadow = 'none';
            }}
            {...props}
          />
          <button
            type="button"
            aria-label={visible ? 'Hide password' : 'Show password'}
            onClick={() => setVisible((v) => !v)}
            className="absolute right-3 top-1/2 -translate-y-1/2 p-0.5"
            style={{ color: 'var(--fg-muted)' }}
          >
            {visible ? <EyeOff style={{ width: 16, height: 16 }} /> : <Eye style={{ width: 16, height: 16 }} />}
          </button>
        </div>
        {error && <p className="text-xs" style={{ color: 'var(--error)' }}>{error}</p>}
        {hint && !error && <p className="text-xs" style={{ color: 'var(--fg-muted)' }}>{hint}</p>}
      </div>
    );
  }
);

PasswordInput.displayName = 'PasswordInput';
export default PasswordInput;
