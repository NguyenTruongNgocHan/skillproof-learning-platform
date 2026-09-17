interface SectionHeadingProps {
  eyebrow?: string;
  title: string;
  subtitle?: string;
  centered?: boolean;
  dark?: boolean;
}

export default function SectionHeading({
  eyebrow,
  title,
  subtitle,
  centered = false,
  dark = false,
}: SectionHeadingProps) {
  return (
    <div className={`${centered ? 'text-center' : ''} mb-12`}>
      {eyebrow && (
        <p
          className="text-xs font-semibold uppercase tracking-widest mb-3"
          style={{ color: '#FF4F8B' }}
        >
          {eyebrow}
        </p>
      )}
      <h2
        className={`text-3xl md:text-4xl font-bold leading-tight ${dark ? 'text-white' : 'text-[#17181C]'}`}
      >
        {title}
      </h2>
      {subtitle && (
        <p
          className={`mt-4 text-lg ${dark ? 'text-gray-300' : 'text-[#666A73]'} ${centered ? 'max-w-2xl mx-auto' : 'max-w-2xl'}`}
        >
          {subtitle}
        </p>
      )}
    </div>
  );
}
