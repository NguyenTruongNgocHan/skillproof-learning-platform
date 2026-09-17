import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";
import LearnerRegistrationForm from "@/features/auth/components/LearnerRegistrationForm";
import type { RegisterLearnerData } from "@/features/auth/types/auth.types";
import { useToast } from "@/components/ui/Toast";

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register, loginWithGoogle } = useAuth();
  const { toast } = useToast();
  const [loading, setLoading] = useState(false);

  async function handleLearnerSubmit(data: RegisterLearnerData) {
    setLoading(true);
    try {
      await register(data);
      toast("success", "Account created. Check your email to activate it.");
      navigate("/verify-email");
    } finally {
      setLoading(false);
    }
  }

  async function handleGoogleClick() {
    setLoading(true);
    try {
      await loginWithGoogle();
      navigate("/verify-email");
    } finally {
      setLoading(false);
    }
  }

  return (
    <LearnerRegistrationForm
      onSubmit={handleLearnerSubmit}
      onGoogleClick={handleGoogleClick}
      onBack={() => navigate("/")}
      loading={loading}
    />
  );
}
