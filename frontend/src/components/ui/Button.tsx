import { ReactNode, ButtonHTMLAttributes } from 'react';

type Variant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'destructive';
type Size = 'sm' | 'md' | 'lg';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: Size;
  children: ReactNode;
  asChild?: boolean;
  className?: string;
}

const variantClasses: Record<Variant, string> = {
  primary:
    'text-white font-medium transition-colors',
  secondary:
    'bg-[#F7F8FA] text-[#17181C] hover:bg-[#E5E7EB] font-medium transition-colors',
  outline:
    'border border-[#E5E7EB] bg-transparent text-[#17181C] hover:bg-[#F7F8FA] font-medium transition-colors',
  ghost:
    'bg-transparent text-[#17181C] hover:bg-[#F7F8FA] font-medium transition-colors',
  destructive:
    'bg-red-600 text-white hover:bg-red-700 font-medium transition-colors',
};

const sizeClasses: Record<Size, string> = {
  sm: 'px-3 py-1.5 text-sm rounded-md',
  md: 'px-4 py-2.5 text-sm rounded-lg',
  lg: 'px-6 py-3 text-base rounded-xl',
};

export default function Button({
  variant = 'primary',
  size = 'md',
  children,
  disabled,
  className = '',
  asChild: _asChild,
  ...props
}: ButtonProps) {
  const isPrimary = variant === 'primary';

  return (
    <button
      {...props}
      disabled={disabled}
      style={isPrimary ? { backgroundColor: '#FF4F8B' } : undefined}
      onMouseEnter={
        isPrimary
          ? (e) => {
              (e.currentTarget as HTMLButtonElement).style.backgroundColor = '#F43F7D';
            }
          : undefined
      }
      onMouseLeave={
        isPrimary
          ? (e) => {
              (e.currentTarget as HTMLButtonElement).style.backgroundColor = '#FF4F8B';
            }
          : undefined
      }
      className={`inline-flex items-center justify-center cursor-pointer ${variantClasses[variant]} ${sizeClasses[size]} ${disabled ? 'opacity-50 cursor-not-allowed' : ''} ${className}`}
    >
      {children}
    </button>
  );
}
