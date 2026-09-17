interface AvatarProps {
  name: string;
  size?: 'sm' | 'md' | 'lg';
  src?: string;
}

const sizes = { sm: 32, md: 40, lg: 48 };

export default function Avatar({ name, size = 'md', src }: AvatarProps) {
  const initials = name.split(' ').map((w) => w[0]).slice(0, 2).join('').toUpperCase();
  const px = sizes[size];

  if (src) {
    return <img src={src} alt={name} style={{ width: px, height: px, borderRadius: '50%', objectFit: 'cover' }} />;
  }

  return (
    <div
      className="flex items-center justify-center font-semibold text-white select-none flex-shrink-0"
      style={{
        width: px,
        height: px,
        borderRadius: '50%',
        background: 'var(--brand)',
        fontSize: size === 'sm' ? 12 : size === 'lg' ? 18 : 14,
      }}
    >
      {initials}
    </div>
  );
}
