interface SkeletonProps {
  className?: string;
  width?: string | number;
  height?: string | number;
  rounded?: string;
}

export default function Skeleton({ className = '', width, height, rounded = 'rounded-md' }: SkeletonProps) {
  return (
    <div
      className={`animate-pulse ${rounded} ${className}`}
      style={{ width, height, background: 'var(--bg-subtle)' }}
    />
  );
}
