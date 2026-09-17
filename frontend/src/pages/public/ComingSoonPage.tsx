import { Link } from 'react-router-dom';
import { ArrowLeft, Clock } from 'lucide-react';
import { primaryLight } from '@/assets/brand';

interface ComingSoonPageProps {
  title: string;
  description?: string;
}

export default function ComingSoonPage({ title, description }: ComingSoonPageProps) {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center px-4" style={{ background: 'var(--bg)' }}>
      <img src={primaryLight} alt="SkillProof" style={{ width: 140, height: 'auto', objectFit: 'contain', marginBottom: 32 }} />
      <div className="w-12 h-12 rounded-xl flex items-center justify-center mb-4" style={{ background: 'var(--bg-subtle)' }}>
        <Clock style={{ width: 24, height: 24, color: 'var(--fg-muted)' }} />
      </div>
      <h1 className="text-2xl font-bold mb-2" style={{ color: 'var(--fg)' }}>{title}</h1>
      {description && <p className="text-base text-center max-w-sm mb-8" style={{ color: 'var(--fg-muted)' }}>{description}</p>}
      <p className="text-sm mb-6" style={{ color: 'var(--fg-muted)' }}>This section is being prepared. Check back soon.</p>
      <Link to="/" className="flex items-center gap-2 text-sm font-medium transition-colors" style={{ color: 'var(--brand)' }}>
        <ArrowLeft style={{ width: 16, height: 16 }} />
        Back to home
      </Link>
    </div>
  );
}
