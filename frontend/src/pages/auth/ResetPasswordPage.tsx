import { useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { AlertCircle, CheckCircle2 } from 'lucide-react';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { useToast } from '@/components/ui/Toast';
import { checkPassword, getPasswordStrength, isPasswordValid } from '@/features/auth/validation/passwordPolicy';
import PasswordRequirements from '@/features/auth/components/PasswordRequirements';
import PasswordInput from '@/components/ui/PasswordInput';
import Button from '@/components/ui/Button';
import LoadingSpinner from '@/components/feedback/LoadingSpinner';

export default function ResetPasswordPage() {
  const { resetPassword } = useAuth();
  const { toast } = useToast();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') ?? '';
  const [password, setPassword] = useState('');
  const [confirmation, setConfirmation] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const checks = useMemo(() => checkPassword(password), [password]);
  const valid = token.length > 0 && isPasswordValid(checks) && password === confirmation;

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!valid) return;
    setError('');
    setLoading(true);
    try {
      await resetPassword(token, password);
      toast('success', 'Password updated. Sign in with your new password.');
      navigate('/login?passwordReset=true', { replace: true });
    } catch (reason: unknown) {
      setError(reason instanceof Error ? reason.message : 'This reset link is invalid or has expired.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center px-4 py-12" style={{ background: 'var(--bg)' }}>
      <section className="w-full max-w-md rounded-2xl border p-8" style={{ background: 'var(--surface)', borderColor: 'var(--border)', boxShadow: 'var(--shadow-lg)' }}>
        {!token ? (
          <div className="text-center">
            <AlertCircle className="mx-auto" size={48} color="var(--error)" />
            <h1 className="mt-4 text-2xl font-bold" style={{ color: 'var(--fg)' }}>Invalid reset link</h1>
            <p className="mt-3 text-sm" style={{ color: 'var(--fg-muted)' }}>Request a new password reset email to continue.</p>
            <Link to="/forgot-password" className="mt-6 inline-block text-sm font-medium" style={{ color: 'var(--brand)' }}>Request a new link</Link>
          </div>
        ) : (
          <>
            <CheckCircle2 size={40} color="var(--brand)" />
            <h1 className="mt-4 text-2xl font-bold" style={{ color: 'var(--fg)' }}>Create a new password</h1>
            <p className="mt-2 text-sm" style={{ color: 'var(--fg-muted)' }}>Choose a strong password you have not used before.</p>
            <form className="mt-7 space-y-4" onSubmit={handleSubmit}>
              <PasswordInput label="New password" autoComplete="new-password" value={password} onChange={(event) => setPassword(event.target.value)} required />
              <PasswordRequirements password={password} checks={checks} strength={getPasswordStrength(password)} />
              <PasswordInput label="Confirm new password" autoComplete="new-password" value={confirmation} onChange={(event) => setConfirmation(event.target.value)} error={confirmation && confirmation !== password ? 'Passwords do not match.' : undefined} required />
              {error && <div className="rounded-lg px-3 py-2 text-sm" style={{ color: 'var(--error)', background: 'color-mix(in srgb, var(--error) 10%, transparent)' }}>{error}</div>}
              <Button className="w-full" type="submit" disabled={!valid || loading}>
                {loading ? <span className="flex items-center gap-2"><LoadingSpinner size={16} />Updating…</span> : 'Reset password'}
              </Button>
            </form>
          </>
        )}
      </section>
    </main>
  );
}
