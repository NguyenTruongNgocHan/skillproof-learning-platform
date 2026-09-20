import { Link } from 'react-router-dom';
import { primaryDark, primaryLight } from '@/assets/brand';
import { useTheme } from '@/app/providers/ThemeProvider';

export default function BrandLogo({ to = '/', compact = false }: { to?: string; compact?: boolean }) {
  const { resolvedTheme } = useTheme();
  return (
    <Link to={to} className="brand-logo" aria-label="SkillProof home">
      <img
        src={resolvedTheme === 'dark' ? primaryDark : primaryLight}
        alt="SkillProof"
        className={compact ? 'brand-logo__image brand-logo__image--compact' : 'brand-logo__image'}
      />
    </Link>
  );
}
