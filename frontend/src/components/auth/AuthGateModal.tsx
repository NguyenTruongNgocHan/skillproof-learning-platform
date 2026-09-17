import Modal from '@/components/ui/Modal';
import Button from '@/components/ui/Button';
import { useNavigate } from 'react-router-dom';
import { ShieldCheck } from 'lucide-react';

interface AuthGateModalProps {
  open: boolean;
  onClose: () => void;
  title?: string;
  description?: string;
  returnPath?: string;
}

export default function AuthGateModal({
  open,
  onClose,
  title = 'Continue with SkillProof',
  description = 'Create an account or sign in to access this content.',
  returnPath,
}: AuthGateModalProps) {
  const navigate = useNavigate();

  const go = (path: string) => {
    onClose();
    navigate(path, returnPath ? { state: { from: returnPath } } : undefined);
  };

  return (
    <Modal open={open} onClose={onClose} maxWidth="400px">
      <div className="flex flex-col items-center text-center gap-4 py-2">
        <div className="w-12 h-12 rounded-xl flex items-center justify-center" style={{ background: 'var(--brand-soft)' }}>
          <ShieldCheck style={{ width: 24, height: 24, color: 'var(--brand)' }} />
        </div>
        <div>
          <h2 className="text-lg font-semibold mb-1" style={{ color: 'var(--fg)' }}>{title}</h2>
          <p className="text-sm" style={{ color: 'var(--fg-muted)' }}>{description}</p>
        </div>
        <div className="flex flex-col gap-2 w-full">
          <Button variant="primary" size="md" className="w-full" onClick={() => go('/register')}>Create Account</Button>
          <Button variant="outline" size="md" className="w-full" onClick={() => go('/login')}>Sign In</Button>
        </div>
        <button onClick={onClose} className="text-sm" style={{ color: 'var(--fg-muted)' }}>Continue Browsing</button>
      </div>
    </Modal>
  );
}
