import { useState, useCallback } from "react";
import LearnerShell from "@/shared/components/layout/LearnerShell";
import { apiClient } from "@/shared/api/apiClient";
import type { Certificate } from "@/features/certification/types/certification.types";
import { useRemote } from "@/shared/hooks/useRemote";
import { RemoteState } from "@/shared/ui/RemoteState";
import { FeedbackPanel } from "../components/FeedbackPanel";
export default function LearnerCertificateFeedbackPage() {
  const [selected, setSelected] = useState("");
  const remote = useRemote(
    useCallback(() => apiClient.get<Certificate[]>("/me/certificates"), []),
  );
  return (
    <LearnerShell>
      <main className="sporg">
        <h1>Certificate experience</h1>
        <p>
          Your public review describes the learning and issuance experience. A
          review does not change credential validity.
        </p>
        <RemoteState {...remote} retry={remote.reload} />
        <label>
          Your certificate
          <select
            value={selected}
            onChange={(e) => setSelected(e.target.value)}
          >
            <option value="">Choose a certificate</option>
            {remote.data?.map((c) => (
              <option key={c.id} value={c.id}>
                {c.programName} · {c.serialNumber} · {c.status}
              </option>
            ))}
          </select>
        </label>
        {remote.data?.length === 0 && <p>No certificates yet.</p>}
        {selected && (
          <FeedbackPanel key={selected} type="CERTIFICATE" target={selected} />
        )}
      </main>
    </LearnerShell>
  );
}
