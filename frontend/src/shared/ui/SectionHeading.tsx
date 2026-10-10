interface SectionHeadingProps {
  eyebrow?: string
  title: string
  subtitle?: string
  centered?: boolean
}

export default function SectionHeading({
  eyebrow,
  title,
  subtitle,
  centered = false,
}: SectionHeadingProps) {
  return (
    <div className={`${centered ? "text-center" : ""} mb-12`}>
      {eyebrow && (
        <p
          className="text-xs font-semibold uppercase tracking-widest mb-3"
          style={{ color: "var(--brand)" }}
        >
          {eyebrow}
        </p>
      )}
      <h2 className="text-3xl md:text-4xl font-bold leading-tight text-skin">{title}</h2>
      {subtitle && (
        <p
          className={`mt-4 text-lg text-muted-skin ${centered ? "max-w-2xl mx-auto" : "max-w-2xl"}`}
        >
          {subtitle}
        </p>
      )}
    </div>
  )
}
