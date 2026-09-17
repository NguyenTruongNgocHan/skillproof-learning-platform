import { CheckCircle, Search, Briefcase, GraduationCap, Building2, Users } from 'lucide-react';
import Container from '@/components/ui/Container';
import SectionHeading from '@/components/ui/SectionHeading';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';

function VerificationPanel() {
  return (
    <div className="space-y-4">
      {/* Input */}
      <div className="flex gap-2">
        <div className="relative flex-1">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[#666A73]" />
          <input
            type="text"
            defaultValue="SP-2025-00847"
            className="w-full pl-9 pr-4 py-2.5 text-sm border border-[#E5E7EB] rounded-lg bg-white text-[#17181C] placeholder:text-[#666A73] outline-none focus:border-[#FF4F8B]"
            placeholder="Enter Certificate ID (e.g. SP-2025-00847)"
          />
        </div>
        <Button variant="primary" size="md">Verify</Button>
      </div>

      {/* Result card */}
      <div className="border border-green-200 bg-green-50 rounded-xl p-5">
        <div className="flex items-center justify-between mb-4">
          <Badge variant="success">
            <CheckCircle size={11} className="mr-1" /> VALID
          </Badge>
          <span className="text-xs text-[#666A73]">Verified just now</span>
        </div>

        <div className="grid grid-cols-2 gap-3">
          {[
            { label: 'Issued by', value: 'SkillProof Technology Academy' },
            { label: 'Holder', value: 'Alex Nguyen' },
            { label: 'Program', value: 'Backend Engineering Foundations' },
            { label: 'Issue Date', value: 'January 15, 2025' },
            { label: 'Status', value: 'Active' },
            { label: 'Certificate ID', value: 'SP-2025-00847' },
          ].map((row) => (
            <div key={row.label}>
              <p className="text-xs text-[#666A73]">{row.label}</p>
              <p className="text-sm font-medium text-[#17181C]">{row.value}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

const users = [
  { icon: Briefcase, title: 'Recruiters', description: 'Confirm candidate credentials before interview' },
  { icon: Building2, title: 'Employers', description: 'Verify skills before extending an offer' },
  { icon: Users, title: 'Training providers', description: 'Confirm prerequisites are met' },
  { icon: GraduationCap, title: 'Educational institutions', description: 'Validate prior learning' },
];

export default function VerificationSection() {
  return (
    <section className="py-24 bg-white">
      <Container>
        <SectionHeading
          title="Verify achievement before you trust the resume."
          subtitle="Recruiters, employers, and institutions can independently verify SkillProof credentials without creating an account."
        />

        <div className="grid md:grid-cols-2 gap-12 lg:gap-20">
          <div>
            <h3 className="font-semibold text-[#17181C] mb-4">Credential Verification</h3>
            <VerificationPanel />
          </div>

          <div>
            <h3 className="font-semibold text-[#17181C] mb-6">Who uses this?</h3>
            <div className="space-y-5">
              {users.map((u) => (
                <div key={u.title} className="flex gap-4">
                  <div
                    className="w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0"
                    style={{ backgroundColor: '#FFF0F5' }}
                  >
                    <u.icon size={16} color="#FF4F8B" />
                  </div>
                  <div>
                    <h4 className="font-semibold text-sm text-[#17181C]">{u.title}</h4>
                    <p className="text-sm text-[#666A73]">{u.description}</p>
                  </div>
                </div>
              ))}
            </div>
            <p className="mt-6 text-sm text-[#666A73]">
              No account required. Simple public URL — anyone can verify.
            </p>
          </div>
        </div>
      </Container>
    </section>
  );
}
