import { InputHTMLAttributes, forwardRef } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  hint?: string;
}

const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, hint, className = '', id, ...props }, ref) => {
    const inputId = id ?? label?.toLowerCase().replace(/\s+/g, '-');
    return (
      <div className="flex flex-col gap-1.5">
        {label && (
          <label htmlFor={inputId} className="text-sm font-medium" style={{ color: 'var(--fg)' }}>
            {label}
          </label>
        )}
        <input
          ref={ref}
          id={inputId}
          className={`w-full px-3.5 py-2.5 rounded-lg text-sm outline-none transition-all ${className}`}
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
        {error && <p className="text-xs" style={{ color: 'var(--error)' }}>{error}</p>}
        {hint && !error && <p className="text-xs" style={{ color: 'var(--fg-muted)' }}>{hint}</p>}
      </div>
    );
  }
);

Input.displayName = 'Input';
export default Input;
