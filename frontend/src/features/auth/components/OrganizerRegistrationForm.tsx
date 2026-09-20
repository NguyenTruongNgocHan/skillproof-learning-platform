import { useState } from "react";
import { Link } from "react-router-dom";
import { ChevronLeft, ArrowRight } from "lucide-react";
import {
  checkPassword,
  isPasswordValid,
  getPasswordStrength,
} from "@/features/auth/validation/passwordPolicy";
import PasswordRequirements from "@/features/auth/components/PasswordRequirements";
import Input from "@/components/ui/Input";
import PasswordInput from "@/components/ui/PasswordInput";
import Button from "@/components/ui/Button";
import Checkbox from "@/components/ui/Checkbox";
import LoadingSpinner from "@/components/feedback/LoadingSpinner";
import type { RegisterOrganizerData } from "@/features/auth/types/auth.types";
import BrandLogo from '@/components/ui/BrandLogo';
import ThemeSwitcher from '@/components/ui/ThemeSwitcher';

const COUNTRIES = [
  "Vietnam",
  "United States",
  "United Kingdom",
  "Singapore",
  "India",
  "Other",
];

interface OrganizerRegistrationFormProps {
  onSubmit: (data: RegisterOrganizerData) => Promise<void>;
  onBack: () => void;
  loading: boolean;
}

export default function OrganizerRegistrationForm({
  onSubmit,
  onBack,
  loading,
}: OrganizerRegistrationFormProps) {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [jobTitle, setJobTitle] = useState("");
  const [organizationName, setOrganizationName] = useState("");
  const [organizationWebsite, setOrganizationWebsite] = useState("");
  const [country, setCountry] = useState("");
  const [agreedToTerms, setAgreedToTerms] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const passwordChecks = checkPassword(password);
  const passwordValid = isPasswordValid(passwordChecks);
  const passwordStrength = getPasswordStrength(password);
  const passwordsMatch = password !== "" && confirmPassword !== "" && password === confirmPassword;
  const confirmError =
    confirmPassword !== "" && !passwordsMatch ? "Passwords do not match." : null;

  const formValid =
    fullName.trim() !== "" &&
    email.trim() !== "" &&
    passwordValid &&
    passwordsMatch &&
    jobTitle.trim() !== "" &&
    organizationName.trim() !== "" &&
    country !== "" &&
    agreedToTerms;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!formValid) return;
    setError(null);
    try {
      await onSubmit({
        fullName,
        email,
        password,
        jobTitle,
        organizationName,
        organizationWebsite,
        country,
      });
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Registration failed. Please try again.");
    }
  }

  return (
    <div
      className="min-h-screen flex items-center justify-center p-6"
      style={{ background: "var(--bg)" }}
    >
      <div className="w-full max-w-md flex flex-col gap-6">
        <div className="flex items-center justify-between"><BrandLogo /><ThemeSwitcher /></div>
        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1 text-sm font-medium w-fit"
          style={{ color: "var(--fg-muted)" }}
        >
          <ChevronLeft size={16} />
          Back
        </button>

        <div>
          <h2 className="text-2xl font-bold" style={{ color: "var(--fg)" }}>
            Create organizer account
          </h2>
          <p className="text-sm mt-1" style={{ color: "var(--fg-muted)" }}>
            Set up your organization on SkillProof
          </p>
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <Input
            label="Full Name"
            type="text"
            autoComplete="name"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            required
          />

          <Input
            label="Work Email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <div className="flex flex-col gap-1">
            <PasswordInput
              label="Password"
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            {password !== "" && (
              <PasswordRequirements
                checks={passwordChecks}
                strength={passwordStrength}
                password={password}
              />
            )}
          </div>

          <PasswordInput
            label="Confirm Password"
            autoComplete="new-password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
            error={confirmError ?? undefined}
          />

          <Input
            label="Job Title"
            type="text"
            autoComplete="organization-title"
            value={jobTitle}
            onChange={(e) => setJobTitle(e.target.value)}
            required
          />

          <Input
            label="Organization Name"
            type="text"
            autoComplete="organization"
            value={organizationName}
            onChange={(e) => setOrganizationName(e.target.value)}
            required
          />

          <Input
            label="Organization Website"
            type="url"
            autoComplete="url"
            placeholder="https://example.com"
            value={organizationWebsite}
            onChange={(e) => setOrganizationWebsite(e.target.value)}
          />

          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium" style={{ color: "var(--fg)" }}>
              Country
            </label>
            <select
              value={country}
              onChange={(e) => setCountry(e.target.value)}
              required
              className="w-full rounded-lg px-3 py-2.5 text-sm border outline-none transition-all"
              style={{
                borderColor: "var(--border)",
                background: "var(--surface)",
                color: country === "" ? "var(--fg-muted)" : "var(--fg)",
              }}
            >
              <option value="" disabled>
                Select your country
              </option>
              {COUNTRIES.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </div>

          <Checkbox
            checked={agreedToTerms}
            onChange={(e) => setAgreedToTerms(e.target.checked)}
            label={
              <span style={{ color: "var(--fg-muted)" }}>
                I agree to the{" "}
                <Link
                  to="/terms"
                  className="font-medium"
                  style={{ color: "var(--brand)" }}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Terms of Service
                </Link>{" "}
                and{" "}
                <Link
                  to="/privacy"
                  className="font-medium"
                  style={{ color: "var(--brand)" }}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Privacy Policy
                </Link>
              </span>
            }
          />

          {error && (
            <div
              className="text-sm rounded-lg px-3 py-2"
              style={{
                color: "var(--error)",
                background: "color-mix(in srgb, var(--error) 10%, transparent)",
              }}
            >
              {error}
            </div>
          )}

          <Button
            type="submit"
            variant="primary"
            className="w-full flex items-center justify-center gap-2"
            disabled={!formValid || loading}
          >
            {loading ? (
              <span className="flex items-center justify-center gap-2">
                <LoadingSpinner size={16} />
                Creating account…
              </span>
            ) : (
              <>
                Continue
                <ArrowRight size={16} />
              </>
            )}
          </Button>
        </form>

        <p className="text-center text-sm" style={{ color: "var(--fg-muted)" }}>
          Already have an account?{" "}
          <Link to="/login" className="font-medium" style={{ color: "var(--brand)" }}>
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
