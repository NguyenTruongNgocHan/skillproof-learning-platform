import { InputHTMLAttributes, ReactNode } from 'react';

interface CheckboxProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label: string | ReactNode;
  error?: string;
}

export default function Checkbox({ label, error, id, className = '', ...props }: CheckboxProps) {
  const inputId = id ?? 'checkbox';
  return (
    <div className={`flex flex-col gap-1 ${className}`}>
      <label htmlFor={inputId} className="flex items-start gap-2.5 cursor-pointer group">
        <div className="relative flex-shrink-0 mt-0.5">
          <input
            id={inputId}
            type="checkbox"
            className="sr-only peer"
            {...props}
          />
          <div
            className="w-4 h-4 rounded flex items-center justify-center transition-all peer-checked:[background:var(--brand)]"
            style={{
              background: 'var(--surface)',
              border: `1px solid ${error ? 'var(--error)' : 'var(--border)'}`,
            }}
          >
            <svg
              className="hidden peer-checked:block"
              width="10"
              height="10"
              viewBox="0 0 10 10"
              fill="none"
            >
              <path d="M2 5l2.5 2.5L8 3" stroke="white" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </div>
        </div>
        <span className="text-sm leading-relaxed" style={{ color: 'var(--fg)' }}>{label}</span>
      </label>
      {error && <p className="text-xs ml-6" style={{ color: 'var(--error)' }}>{error}</p>}
    </div>
  );
}
