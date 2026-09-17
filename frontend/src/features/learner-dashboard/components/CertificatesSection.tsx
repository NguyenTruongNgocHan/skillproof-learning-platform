import { Award } from "lucide-react";
import EmptyState from "@/components/feedback/EmptyState";

export default function CertificatesSection() {
  return (
    <div>
      <h2
        style={{
          fontSize: "1rem",
          fontWeight: "700",
          color: "var(--fg)",
          margin: "0 0 14px",
        }}
      >
        Certificates
      </h2>
      <EmptyState
        icon={Award}
        title="No certificates yet"
        description="Complete a learning path to earn your first certificate."
      />
    </div>
  );
}
