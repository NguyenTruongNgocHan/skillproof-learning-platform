import { ReactNode } from 'react';

type Variant = 'default' | 'success' | 'warning' | 'error' | 'brand' | 'outline';

interface BadgeProps {
  variant?: Variant;
  children: ReactNode;
  className?: string;
}

const variantClasses: Record<Variant, string> = {
  default: 'bg-gray-100 text-gray-700',
  success: 'bg-green-50 text-green-700',
  warning: 'bg-yellow-50 text-yellow-700',
  error: 'bg-red-50 text-red-600',
  brand: 'text-[#FF4F8B]',
  outline: 'border border-[#E5E7EB] bg-transparent text-[#666A73]',
};

export default function Badge({ variant = 'default', children, className = '' }: BadgeProps) {
  const isBrand = variant === 'brand';
  return (
    <span
      style={isBrand ? { backgroundColor: '#FFF0F5' } : undefined}
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${variantClasses[variant]} ${className}`}
    >
      {children}
    </span>
  );
}
