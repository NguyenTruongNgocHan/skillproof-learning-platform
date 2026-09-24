import { GraduationCap, Building2, ScanLine } from "lucide-react";
import Container from "@/components/ui/Container";

const audiences = [
  { icon: GraduationCap, label: "Learn with a clear goal" },
  { icon: Building2, label: "Build organization programs" },
  { icon: ScanLine, label: "Verify credentials independently" },
];

export default function TrustSection() {
  return (
    <section className="marketing-audiences" aria-label="SkillProof audiences">
      <Container>
        <p className="eyebrow">
          ONE CONNECTED JOURNEY
        </p>

        <div className="marketing-audience-grid">
          {audiences.map(({ icon: Icon, label }) => (
            <div className="marketing-audience" key={label}>
              <span className="marketing-audience-icon">
                <Icon size={22} aria-hidden="true" />
              </span>
              <span>{label}</span>
            </div>
          ))}
        </div>
      </Container>
    </section>
  );
}