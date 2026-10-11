import { useState, useCallback } from "react";
import AppShell from "@/shared/components/layout/AppShell";
import { apiClient } from "@/shared/api/apiClient";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import type { Certificate } from "@/features/certification/types/certification.types";
import { CertificateProvenancePanel } from "@/features/certification/components/CertificateProvenancePanel";
import Input from "@/shared/ui/Input";
import Button from "@/shared/ui/Button";
export default function AdminCertificateGovernancePage() {
  const [lookup, setLookup] = useState(""),
    [id, setId] = useState(""),
    [reason, setReason] = useState("");
  const remote = useRemote(
    useCallback(
      () =>
        id
          ? apiClient.get<Certificate>(
              `/admin/certificates/${encodeURIComponent(id)}`,
            )
          : Promise.resolve(null),
      [id],
    ),
  );
  const action = useAction(remote.reload);
  return (
    <AppShell>
      <main className="sporg">
        <h1>Certificate governance</h1>
        <p>
          Platform administrators can revoke fraudulent credentials even when
          their organization is suspended. This action retains the certificate
          and its original provenance.
        </p>
        <form
          className="sporg-toolbar"
          onSubmit={(e) => {
            e.preventDefault();
            setId(lookup.trim());
          }}
        >
          <Input
            label="Certificate UUID"
            required
            value={lookup}
            onChange={(e) => setLookup(e.target.value)}
          />
          <Button type="submit">Find certificate</Button>
        </form>
        <RemoteState {...remote} retry={remote.reload} />
        <ActionNotice {...action} />
        {remote.data && (
          <section className="sporg-card">
            <h2>{remote.data.programName}</h2>
            <p>
              {remote.data.serialNumber} · {remote.data.status}
            </p>
            <p>
              {remote.data.issuerName} · {remote.data.learnerEmail}
            </p>
            <CertificateProvenancePanel serial={remote.data.serialNumber} />
            <Input
              label="Revocation reason"
              required
              maxLength={1000}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
            />
            <Button
              variant="destructive"
              disabled={
                action.busy ||
                !reason.trim() ||
                remote.data.status === "REVOKED"
              }
              onClick={() => {
                if (window.confirm("Revoke this credential permanently?"))
                  void action.run(
                    () =>
                      apiClient.post(`/admin/certificates/${id}/revoke`, {
                        reason,
                      }),
                    "Certificate revoked.",
                  );
              }}
            >
              Revoke certificate
            </Button>
          </section>
        )}
      </main>
    </AppShell>
  );
}
