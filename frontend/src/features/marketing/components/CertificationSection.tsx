import { CheckCircle, ShieldCheck } from 'lucide-react';
import Container from '@/components/ui/Container';
import SectionHeading from '@/components/ui/SectionHeading';
import Badge from '@/components/ui/Badge';

function CertificateCard() {
  return (
    <div className="bg-white rounded-2xl border border-[#E5E7EB] overflow-hidden shadow-sm">
      <div className="grid md:grid-cols-2">
        {/* Left: credential details */}
        <div className="p-8 border-r border-[#E5E7EB]">
          <p
            className="text-xs font-semibold uppercase tracking-widest mb-5"
            style={{ color: '#FF4F8B' }}
          >
            Certificate of Achievement
          </p>

          <div className="space-y-3">
            {[
              { label: 'Learner', value: 'Alex Nguyen' },
              { label: 'Program', value: 'Backend Engineering Foundations' },
              { label: 'Issuing Organization', value: 'SkillProof Technology Academy' },
              { label: 'Completed', value: 'January 15, 2025' },
              { label: 'Certificate ID', value: 'SP-2025-00847' },
            ].map((row) => (
              <div key={row.label}>
                <p className="text-xs text-[#666A73]">{row.label}</p>
                <p className="text-sm font-medium text-[#17181C]">{row.value}</p>
              </div>
            ))}

            <div>
              <p className="text-xs text-[#666A73] mb-1">Status</p>
              <Badge variant="success">
                <CheckCircle size={11} className="mr-1" /> VALID
              </Badge>
            </div>
          </div>
        </div>

        {/* Right: verification trust */}
        <div className="p-8 flex flex-col" style={{ backgroundColor: '#F7F8FA' }}>
          <div className="flex items-center gap-2 mb-4">
            <ShieldCheck size={20} color="#FF4F8B" />
            <h3 className="font-semibold text-[#17181C]">Independently Verifiable</h3>
          </div>

          <div className="space-y-2.5 mb-6">
            {[
              'Issuer verified',
              'Credential integrity checked',
              'Public verification URL',
              'Revocation status checked',
            ].map((point) => (
              <div key={point} className="flex items-center gap-2">
                <CheckCircle size={14} color="#22c55e" />
                <span className="text-sm text-[#17181C]">{point}</span>
              </div>
            ))}
          </div>

          <p
            className="text-xs font-mono text-[#666A73] break-all mt-auto"
          >
            skillproof.io/verify/SP-2025-00847
          </p>
        </div>
      </div>
    </div>
  );
}

export default function CertificationSection() {
  return (
    <section className="py-24" style={{ backgroundColor: '#F7F8FA' }}>
      <Container>
        <SectionHeading
          title="Your achievement should be verifiable."
          subtitle="Certificates are issued by approved organizations after learners meet published completion requirements. Each credential can be independently verified through SkillProof's public verification experience."
        />
        <CertificateCard />
      </Container>
    </section>
  );
}
