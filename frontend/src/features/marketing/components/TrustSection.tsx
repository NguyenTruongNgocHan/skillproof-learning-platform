import Container from '@/components/ui/Container';

const orgs = [
  'Technology Academy',
  'Professional Institute',
  'Engineering School',
  'QA Training Institute',
  'Cloud Training Co',
];

export default function TrustSection() {
  return (
    <section className="py-12" style={{ backgroundColor: '#F7F8FA' }}>
      <Container>
        <p className="text-center text-sm text-[#666A73] mb-8 font-medium">
          Built for learners, training organizations, and credential verification.
        </p>
        <div className="flex flex-wrap justify-center gap-4">
          {orgs.map((org) => (
            <div
              key={org}
              className="px-5 py-2.5 rounded-lg border border-[#E5E7EB] bg-white"
            >
              <span className="text-sm font-medium text-gray-500">{org}</span>
            </div>
          ))}
        </div>
      </Container>
    </section>
  );
}
