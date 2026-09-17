interface ProgressProps {
  value: number;
  size?: 'sm' | 'md';
  color?: 'brand' | 'success';
  className?: string;
  showLabel?: boolean;
}

export default function Progress({
  value,
  size = 'md',
  color = 'brand',
  className = '',
  showLabel = false,
}: ProgressProps) {
  const clampedValue = Math.min(100, Math.max(0, value));
  const trackHeight = size === 'sm' ? '4px' : '8px';
  const fillColor = color === 'brand' ? '#FF4F8B' : '#16a34a';

  return (
    <div className={`w-full ${className}`}>
      <div
        className="w-full rounded-full overflow-hidden bg-gray-100"
        style={{ height: trackHeight }}
      >
        <div
          className="h-full rounded-full transition-all duration-500"
          style={{ width: `${clampedValue}%`, backgroundColor: fillColor }}
        />
      </div>
      {showLabel && (
        <p className="mt-1 text-xs text-[#666A73]">{clampedValue}%</p>
      )}
    </div>
  );
}
