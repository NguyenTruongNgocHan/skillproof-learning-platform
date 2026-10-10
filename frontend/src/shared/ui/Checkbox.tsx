import { InputHTMLAttributes, ReactNode, useId } from "react"

interface CheckboxProps extends Omit<InputHTMLAttributes<HTMLInputElement>, "type"> {
  label: string | ReactNode
  error?: string
}

export default function Checkbox({
  label,
  error,
  id,
  className = "",
  disabled,
  ...props
}: CheckboxProps) {
  const generatedId = useId()
  const inputId = id ?? generatedId

  return (
    <div className={`flex flex-col gap-1 ${className}`}>
      <label
        htmlFor={inputId}
        className={`flex items-start gap-2.5 group ${
          disabled ? "cursor-not-allowed opacity-60" : "cursor-pointer"
        }`}
      >
        <span className="relative mt-0.5 flex h-4 w-4 flex-shrink-0">
          <input
            id={inputId}
            type="checkbox"
            disabled={disabled}
            className="peer absolute inset-0 h-4 w-4 cursor-pointer opacity-0 disabled:cursor-not-allowed"
            {...props}
          />

          <span
            aria-hidden="true"
            className="
              pointer-events-none
              flex h-4 w-4 items-center justify-center
              rounded
              border
              transition-all
              peer-focus-visible:ring-2
              peer-focus-visible:ring-[var(--brand)]
              peer-focus-visible:ring-offset-2
              peer-focus-visible:ring-offset-[var(--bg)]
              peer-checked:border-[var(--brand)]
              peer-checked:bg-[var(--brand)]
            "
            style={{
              backgroundColor: "var(--surface)",
              borderColor: error ? "var(--error)" : "var(--border)",
            }}
          >
            <svg
              className="
                hidden
                h-2.5 w-2.5
                peer-checked:[&]:block
              "
              viewBox="0 0 10 10"
              fill="none"
            >
              <path
                d="M2 5l2.5 2.5L8 3"
                stroke="white"
                strokeWidth="1.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </span>
        </span>

        <span className="text-sm leading-relaxed" style={{ color: "var(--fg)" }}>
          {label}
        </span>
      </label>

      {error && (
        <p className="ml-6 text-xs" style={{ color: "var(--error)" }} role="alert">
          {error}
        </p>
      )}
    </div>
  )
}
