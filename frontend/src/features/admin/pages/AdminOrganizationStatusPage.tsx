import { useState, useCallback } from "react";
import type { Organization } from "@/features/organization/types/organization.types";
import { apiClient } from "@/shared/api/apiClient";
import AppShell from "@/shared/components/layout/AppShell";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Input from "@/shared/ui/Input";
import Button from "@/shared/ui/Button";
export default function AdminOrganizationStatusPage() {
  const [lookup, setLookup] = useState(""),
    [id, setId] = useState(""),
    [reason, setReason] = useState("");
  const remote = useRemote(
    useCallback(
      () =>
        id
          ? apiClient.get<Organization>(
              `/admin/organization-status/${encodeURIComponent(id)}`,
            )
          : Promise.resolve(null),
      [id],
    ),
  );
  const action = useAction(remote.reload);
  return (
    <AppShell>
      <main className="sporg">
        <header className="sporg-heading">
          <h1>Organization governance</h1>
          <p>
            Suspension blocks new enrollment, invitations and issuance.
            Historical records are retained. Restoration does not reactivate a
            revoked issuer authorization.
          </p>
        </header>
        <form
          className="sporg-toolbar"
          onSubmit={(e) => {
            e.preventDefault();
            setId(lookup.trim());
          }}
        >
          <Input
            label="Organization UUID"
            required
            value={lookup}
            onChange={(e) => setLookup(e.target.value)}
          />
          <Button type="submit">Find organization</Button>
        </form>
        <RemoteState {...remote} retry={remote.reload} />
        <ActionNotice {...action} />
        {remote.data && (
          <section className="sporg-card">
            <h2>{remote.data.displayName}</h2>
            <p>
              {remote.data.legalName} · {remote.data.status}
            </p>
            <p>{remote.data.reviewReason}</p>
            <Input
              label="Decision reason"
              required
              maxLength={1000}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
            />
            <div className="sporg-actions">
              {[true, false].map((suspended) => (
                <Button
                  key={String(suspended)}
                  variant={suspended ? "destructive" : "outline"}
                  disabled={
                    action.busy ||
                    !reason.trim() ||
                    remote.data?.status !==
                      (suspended ? "APPROVED" : "SUSPENDED")
                  }
                  onClick={() => {
                    if (
                      window.confirm(
                        suspended
                          ? "Suspend this organization?"
                          : "Restore organization access?",
                      )
                    )
                      void action.run(
                        () =>
                          apiClient.put(
                            `/admin/organization-status/${encodeURIComponent(id)}`,
                            { suspended, reason },
                          ),
                        "Organization status updated.",
                      );
                  }}
                >
                  {suspended ? "Suspend organization" : "Restore organization"}
                </Button>
              ))}
            </div>
          </section>
        )}
      </main>
    </AppShell>
  );
}
