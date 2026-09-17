import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { UploadCloud, CheckCircle, Circle } from 'lucide-react';
import { useAuth } from '@/features/auth/hooks/useAuth';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import { storage, KEYS } from '@/services/storage/storage';

const INDUSTRIES = [
  'EdTech',
  'Professional Training',
  'University',
  'Government',
  'Corporate L&D',
  'Non-profit',
  'Other',
];

const COUNTRIES = [
  'United States',
  'United Kingdom',
  'Canada',
  'Australia',
  'Germany',
  'India',
];

interface ProgressDotsProps {
  total: number;
  current: number;
}

function ProgressDots({ total, current }: ProgressDotsProps) {
  return (
    <div className="flex items-center gap-2 justify-center mb-8">
      {Array.from({ length: total }, (_, i) => {
        const step = i + 1;
        const filled = step <= current;
        return filled ? (
          <CheckCircle key={i} size={20} style={{ color: 'var(--brand)' }} />
        ) : (
          <Circle key={i} size={20} style={{ color: 'var(--border)' }} />
        );
      })}
    </div>
  );
}

interface ReviewRowProps {
  label: string;
  value: string;
}

function ReviewRow({ label, value }: ReviewRowProps) {
  return (
    <div className="flex justify-between items-start gap-4 py-2" style={{ borderBottom: '1px solid var(--border)' }}>
      <span className="text-sm" style={{ color: 'var(--fg-muted)' }}>{label}</span>
      <span className="text-sm font-medium text-right" style={{ color: 'var(--fg)' }}>{value || '—'}</span>
    </div>
  );
}

interface ReviewSectionProps {
  title: string;
  children: React.ReactNode;
}

function ReviewSection({ title, children }: ReviewSectionProps) {
  return (
    <div className="flex flex-col gap-1">
      <p className="text-xs font-semibold uppercase tracking-wider mb-1" style={{ color: 'var(--fg-muted)' }}>
        {title}
      </p>
      {children}
    </div>
  );
}

export default function OrganizerOnboardingPage() {
  const { user, completeOrganizerOnboarding } = useAuth();
  const navigate = useNavigate();

  const [currentStep, setCurrentStep] = useState(1);
  const [submitted, setSubmitted] = useState(false);

  // Step 1
  const [legalName, setLegalName] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [website, setWebsite] = useState('');
  const [industry, setIndustry] = useState('');

  // Step 2
  const [fullName, setFullName] = useState(user?.fullName ?? '');
  const [jobTitle, setJobTitle] = useState('');
  const [workEmail, setWorkEmail] = useState(user?.email ?? '');
  const [phone, setPhone] = useState('');

  // Step 3
  const [regNumber, setRegNumber] = useState('');
  const [officialDomain, setOfficialDomain] = useState('');
  const [street, setStreet] = useState('');
  const [city, setCity] = useState('');
  const [country, setCountry] = useState('');

  const selectStyle: React.CSSProperties = {
    background: 'var(--surface)',
    color: 'var(--fg)',
    border: '1px solid var(--border)',
    borderRadius: 8,
    padding: '10px 14px',
    fontSize: 14,
    width: '100%',
    outline: 'none',
  };

  const handleSubmit = () => {
    storage.set(KEYS.ORGANIZER_PROFILE, { verificationStatus: 'PENDING' as const, organizationName: legalName || displayName, jobTitle });
    setSubmitted(true);
  };

  const handleContinue = async () => {
    await completeOrganizerOnboarding();
    navigate('/organizer/verification-pending');
  };

  if (submitted) {
    return (
      <div
        className="min-h-screen flex items-center justify-center px-4 py-12"
        style={{ background: 'var(--bg)' }}
      >
        <div
          className="w-full max-w-lg rounded-2xl shadow p-8 flex flex-col items-center gap-6 text-center"
          style={{ background: 'var(--surface)' }}
        >
          <CheckCircle size={48} style={{ color: 'var(--success)' }} />
          <div className="flex flex-col gap-2">
            <h1 className="text-2xl font-bold" style={{ color: 'var(--fg)' }}>
              Application Submitted!
            </h1>
            <p className="text-sm" style={{ color: 'var(--fg-muted)' }}>
              Organization verification pending. Our team will review within 2-3 business days.
            </p>
          </div>
          <Button variant="primary" size="lg" className="w-full" onClick={handleContinue}>
            Continue to Organizer Workspace
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div
      className="min-h-screen flex items-center justify-center px-4 py-12"
      style={{ background: 'var(--bg)' }}
    >
      <div
        className="w-full max-w-lg rounded-2xl shadow p-8"
        style={{ background: 'var(--surface)' }}
      >
        <ProgressDots total={4} current={currentStep} />

        {/* STEP 1 — Organization Profile */}
        {currentStep === 1 && (
          <div className="flex flex-col gap-5">
            <h1 className="text-xl font-bold" style={{ color: 'var(--fg)' }}>
              Tell us about your organization
            </h1>
            <Input
              label="Legal Organization Name"
              value={legalName}
              onChange={(e) => setLegalName(e.target.value)}
              placeholder="Acme Corp Inc."
            />
            <Input
              label="Display Name"
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              placeholder="Acme Corp"
            />
            <Input
              label="Organization Website"
              type="url"
              value={website}
              onChange={(e) => setWebsite(e.target.value)}
              placeholder="https://acme.com"
            />
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium" style={{ color: 'var(--fg)' }}>
                Industry
              </label>
              <select
                value={industry}
                onChange={(e) => setIndustry(e.target.value)}
                style={selectStyle}
              >
                <option value="">Select industry</option>
                {INDUSTRIES.map((ind) => (
                  <option key={ind} value={ind}>{ind}</option>
                ))}
              </select>
            </div>
            <div className="flex gap-3 mt-2">
              <Button variant="outline" size="lg" className="flex-1" onClick={() => navigate('/register')}>
                Back
              </Button>
              <Button
                variant="primary"
                size="lg"
                className="flex-1"
                disabled={!legalName || !displayName || !industry}
                onClick={() => setCurrentStep(2)}
              >
                Next
              </Button>
            </div>
          </div>
        )}

        {/* STEP 2 — Representative Info */}
        {currentStep === 2 && (
          <div className="flex flex-col gap-5">
            <h1 className="text-xl font-bold" style={{ color: 'var(--fg)' }}>
              Your contact information
            </h1>
            <Input
              label="Full Name"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="Jane Smith"
            />
            <Input
              label="Job Title"
              value={jobTitle}
              onChange={(e) => setJobTitle(e.target.value)}
              placeholder="Learning & Development Manager"
            />
            <Input
              label="Work Email"
              type="email"
              value={workEmail}
              onChange={(e) => setWorkEmail(e.target.value)}
              placeholder="jane@acme.com"
            />
            <Input
              label="Business Phone"
              type="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder="+1 (555) 000-0000"
            />
            <div className="flex gap-3 mt-2">
              <Button variant="outline" size="lg" className="flex-1" onClick={() => setCurrentStep(1)}>
                Back
              </Button>
              <Button
                variant="primary"
                size="lg"
                className="flex-1"
                disabled={!fullName || !jobTitle || !workEmail}
                onClick={() => setCurrentStep(3)}
              >
                Next
              </Button>
            </div>
          </div>
        )}

        {/* STEP 3 — Verification Documents */}
        {currentStep === 3 && (
          <div className="flex flex-col gap-5">
            <h1 className="text-xl font-bold" style={{ color: 'var(--fg)' }}>
              Verification information
            </h1>
            <Input
              label="Business Registration Number"
              value={regNumber}
              onChange={(e) => setRegNumber(e.target.value)}
              placeholder="EIN / CRN / VAT number"
            />
            <Input
              label="Official Domain"
              value={officialDomain}
              onChange={(e) => setOfficialDomain(e.target.value)}
              placeholder="company.com"
            />
            <Input
              label="Street Address"
              value={street}
              onChange={(e) => setStreet(e.target.value)}
              placeholder="123 Business Ave"
            />
            <Input
              label="City"
              value={city}
              onChange={(e) => setCity(e.target.value)}
              placeholder="San Francisco"
            />
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium" style={{ color: 'var(--fg)' }}>
                Country
              </label>
              <select
                value={country}
                onChange={(e) => setCountry(e.target.value)}
                style={selectStyle}
              >
                <option value="">Select country</option>
                {COUNTRIES.map((c) => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </div>

            {/* Document upload placeholder */}
            <div
              className="flex flex-col items-center justify-center gap-2 rounded-lg p-6 text-center"
              style={{ border: '1.5px dashed var(--border)' }}
            >
              <UploadCloud size={32} style={{ color: 'var(--fg-muted)' }} />
              <p className="text-sm font-medium" style={{ color: 'var(--fg-muted)' }}>
                Document upload coming soon
              </p>
              <p className="text-xs" style={{ color: 'var(--fg-muted)' }}>
                Business registration certificate, license, or official letterhead
              </p>
            </div>

            <div className="flex gap-3 mt-2">
              <Button variant="outline" size="lg" className="flex-1" onClick={() => setCurrentStep(2)}>
                Back
              </Button>
              <Button
                variant="primary"
                size="lg"
                className="flex-1"
                disabled={!regNumber || !officialDomain || !street || !city || !country}
                onClick={() => setCurrentStep(4)}
              >
                Review Application
              </Button>
            </div>
          </div>
        )}

        {/* STEP 4 — Review */}
        {currentStep === 4 && (
          <div className="flex flex-col gap-6">
            <h1 className="text-xl font-bold" style={{ color: 'var(--fg)' }}>
              Review your application
            </h1>

            <ReviewSection title="Organization Info">
              <ReviewRow label="Legal Name" value={legalName} />
              <ReviewRow label="Display Name" value={displayName} />
              <ReviewRow label="Website" value={website} />
              <ReviewRow label="Industry" value={industry} />
            </ReviewSection>

            <ReviewSection title="Representative Info">
              <ReviewRow label="Full Name" value={fullName} />
              <ReviewRow label="Job Title" value={jobTitle} />
              <ReviewRow label="Work Email" value={workEmail} />
              <ReviewRow label="Business Phone" value={phone} />
            </ReviewSection>

            <ReviewSection title="Verification">
              <ReviewRow label="Registration Number" value={regNumber} />
              <ReviewRow label="Official Domain" value={officialDomain} />
              <ReviewRow label="Street Address" value={street} />
              <ReviewRow label="City" value={city} />
              <ReviewRow label="Country" value={country} />
            </ReviewSection>

            <div className="flex flex-col gap-3 mt-2">
              <Button variant="primary" size="lg" className="w-full" onClick={handleSubmit}>
                Submit for Review
              </Button>
              <Button variant="outline" size="lg" className="w-full" onClick={() => setCurrentStep(3)}>
                Back
              </Button>
              <p className="text-xs text-center" style={{ color: 'var(--fg-muted)' }}>
                Our team will review your application within 2-3 business days.
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
