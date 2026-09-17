import { LucideIcon } from 'lucide-react';
import Button from '@/components/ui/Button';

interface EmptyStateProps {
  icon: LucideIcon;
  title: string;
  description?: string;
  action?: { label: string; onClick: () => void };
}

export default function EmptyState({ icon: Icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-16 px-4 text-center">
      <div className="w-12 h-12 rounded-xl flex items-center justify-center mb-4" style={{ background: 'var(--bg-subtle)' }}>
        <Icon style={{ width: 24, height: 24, color: 'var(--fg-muted)' }} />
      </div>
      <h3 className="text-base font-semibold mb-1" style={{ color: 'var(--fg)' }}>{title}</h3>
      {description && <p className="text-sm max-w-xs" style={{ color: 'var(--fg-muted)' }}>{description}</p>}
      {action && <div className="mt-4"><Button variant="primary" size="sm" onClick={action.onClick}>{action.label}</Button></div>}
    </div>
  );
}
