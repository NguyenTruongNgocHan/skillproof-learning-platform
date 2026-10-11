import { useCallback } from "react";
import { Link } from "react-router-dom";
import { apiClient } from "@/shared/api/apiClient";
import { useRemote } from "@/shared/hooks/useRemote";
import { RemoteState } from "@/shared/ui/RemoteState";
export function CertificateProvenancePanel({ serial }: { serial: string }) {
  const remote = useRemote(
    useCallback(
      () =>
        apiClient.get<{
          organizationId: string;
          issuerApplicationId?: string;
          credentialHash?: string;
          proofStatus: string;
        }>(`/public/certificates/${encodeURIComponent(serial)}/provenance`),
      [serial],
    ),
  );
  return (
    <section>
      <h3>Issuer provenance</h3>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data && (
        <>
          <Link to={`/organizations/${remote.data.organizationId}`}>
            View originating organization →
          </Link>
          <p>
            Authorization record:{" "}
            {remote.data.issuerApplicationId ??
              "Historical provenance unavailable"}
          </p>
          {remote.data.credentialHash && (
            <p>
              Credential SHA-256:{" "}
              <code style={{ overflowWrap: "anywhere" }}>
                {remote.data.credentialHash}
              </code>
            </p>
          )}
          <p>
            {remote.data.proofStatus} · No blockchain transaction exists in this
            phase.
          </p>
        </>
      )}
    </section>
  );
}
