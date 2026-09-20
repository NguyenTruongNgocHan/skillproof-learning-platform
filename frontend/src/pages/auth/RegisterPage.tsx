import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/features/auth/hooks/useAuth';
import LearnerRegistrationForm from '@/features/auth/components/LearnerRegistrationForm';
import OrganizerRegistrationForm from '@/features/auth/components/OrganizerRegistrationForm';
import RoleSelection from '@/features/auth/components/RoleSelection';
import type { RegisterLearnerData, RegisterOrganizerData } from '@/features/auth/types/auth.types';
import { useToast } from '@/components/ui/Toast';

type RegistrationRole = 'LEARNER' | 'ORGANIZER';

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register, loginWithGoogle } = useAuth();
  const { toast } = useToast();
  const [selectedRole, setSelectedRole] = useState<RegistrationRole | null>(null);
  const [step, setStep] = useState<'role' | 'details'>('role');
  const [loading, setLoading] = useState(false);

  async function submit(data: RegisterLearnerData | RegisterOrganizerData) {
    if (!selectedRole) return;
    setLoading(true);
    try {
      await register({ ...data, role: selectedRole });
      toast('success', 'Account created. Check your inbox to verify your email.');
      navigate('/verify-email', { state: { email: data.email } });
    } finally { setLoading(false); }
  }

  async function google() {
    setLoading(true);
    try { await loginWithGoogle(); } finally { setLoading(false); }
  }

  if (step === 'role') return <RoleSelection selected={selectedRole} onSelect={setSelectedRole} onContinue={() => setStep('details')} />;
  if (selectedRole === 'ORGANIZER') return <OrganizerRegistrationForm onSubmit={submit} onBack={() => setStep('role')} loading={loading} />;
  return <LearnerRegistrationForm onSubmit={submit} onGoogleClick={google} onBack={() => setStep('role')} loading={loading} />;
}
