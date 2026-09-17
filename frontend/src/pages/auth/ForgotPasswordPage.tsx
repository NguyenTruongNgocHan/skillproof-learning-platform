import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { ArrowLeft, MailCheck } from 'lucide-react';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { useToast } from '@/components/ui/Toast';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import LoadingSpinner from '@/components/feedback/LoadingSpinner';

export default function ForgotPasswordPage() {
  const { forgotPassword } = useAuth();
  const { toast } = useToast();
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    try {
      await forgotPassword(email.trim());
      setSubmitted(true);
      toast('success', 'Check your inbox for the next step.');
    } catch (error: unknown) {
      toast('error', error instanceof Error ? error.message : 'The request could not be completed.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center px-4 py-12" style={{ background: 'var(--bg)' }}>
      <section className="w-full max-w-md rounded-2xl border p-8" style={{ background: 'var(--surface)', borderColor: 'var(--border)', boxShadow: 'var(--shadow-lg)' }}>
        {submitted ? (
          <div className="text-center">
            <MailCheck className="mx-auto" size={48} color="var(--brand)" />
            <h1 className="mt-4 text-2xl font-bold" style={{ color: 'var(--fg)' }}>Check your email</h1>
            <p className="mt-3 text-sm leading-6" style={{ color: 'var(--fg-muted)' }}>
              If an eligible SkillProof account exists for that address, we sent a secure password reset link. It expires after 30 minutes.
            </p>
            <Button className="mt-7 w-full" variant="outline" onClick={() => setSubmitted(false)}>
              Send another request
            </Button>
          </div>
        ) : (
          <>
            <h1 className="text-2xl font-bold" style={{ color: 'var(--fg)' }}>Forgot your password?</h1>
            <p className="mt-2 text-sm leading-6" style={{ color: 'var(--fg-muted)' }}>
              Enter your account email and we will send you a secure reset link.
            </p>
            <form className="mt-7 space-y-5" onSubmit={handleSubmit}>
              <Input label="Email address" type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
              <Button className="w-full" type="submit" disabled={loading}>
                {loading ? <span className="flex items-center gap-2"><LoadingSpinner size={16} />Sending…</span> : 'Send reset link'}
              </Button>
            </form>
          </>
        )}
        <Link to="/login" className="mt-6 flex items-center justify-center gap-2 text-sm font-medium" style={{ color: 'var(--brand)' }}>
          <ArrowLeft size={16} /> Back to sign in
        </Link>
      </section>
    </main>
  );
}
