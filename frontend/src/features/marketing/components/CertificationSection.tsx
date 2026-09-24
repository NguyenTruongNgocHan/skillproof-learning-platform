import { CheckCircle, ShieldCheck } from "lucide-react";
import Container from "@/components/ui/Container";
import SectionHeading from "@/components/ui/SectionHeading";
import Badge from "@/components/ui/Badge";

function CertificateCard() {
  return (
    <div className="bg-skin rounded-2xl border border-skin overflow-hidden shadow-sm">
      <div className="grid md:grid-cols-2">
        {/* Left: credential details */}
        <div className="p-8 border-r border-skin">
          <p
            className="text-xs font-semibold uppercase tracking-widest mb-5"
            style={{ color: "var(--brand)" }}
          >
            ILLUSTRATIVE CREDENTIAL · NOT ISSUED
          </p>

          <div className="space-y-3">
            {[
              { label: "Learner", value: "Example learner" },
              { label: "Program", value: "Backend Engineering Foundations" },
              {
                label: "Issuing Organization",
                value: "Example approved organization",
              },
              { label: "Completed", value: "After requirements are met" },
              { label: "Certificate ID", value: "Assigned at issuance" },
            ].map((row) => (
              <div key={row.label}>
                <p className="text-xs text-muted-skin">{row.label}</p>
                <p className="text-sm font-medium text-skin">{row.value}</p>
              </div>
            ))}

            <div>
              <p className="text-xs text-muted-skin mb-1">Status</p>
              <Badge variant="success">
                <CheckCircle size={11} className="mr-1" /> Example
              </Badge>
            </div>
          </div>
        </div>

        {/* Right: verification trust */}
        <div
          className="p-8 flex flex-col"
          style={{ backgroundColor: "var(--bg-subtle)" }}
        >
          <div className="flex items-center gap-2 mb-4">
            <ShieldCheck size={20} color="var(--brand)" />
            <h3 className="font-semibold text-skin">
              Independently Verifiable
            </h3>
          </div>

          <div className="space-y-2.5 mb-6">
            {[
              "Issuer verified",
              "Credential integrity checked",
              "Public verification URL",
              "Revocation status checked",
            ].map((point) => (
              <div key={point} className="flex items-center gap-2">
                <CheckCircle size={14} color="#22c55e" />
                <span className="text-sm text-skin">{point}</span>
              </div>
            ))}
          </div>

          <p className="text-xs font-mono text-muted-skin break-all mt-auto">
            Public verification URL will appear after issuance
          </p>
        </div>
      </div>
    </div>
  );
}

export default function CertificationSection() {
  return (
    <section className="py-24" style={{ backgroundColor: "var(--bg-subtle)" }}>
      <Container>
        <SectionHeading
          title="Your achievement should be verifiable."
          subtitle="Certificates are issued by approved organizations after learners meet published completion requirements. Public verification is part of the certification roadmap and is not live in this release."
        />
        <CertificateCard />
      </Container>
    </section>
  );
}
