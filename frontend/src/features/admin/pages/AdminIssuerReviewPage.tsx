import { useCallback, useState } from "react";
import AppShell from "@/shared/components/layout/AppShell";
import { governanceApi } from "@/features/organization/api/governanceApi";
import { EvidenceManager } from "@/features/organization/components/EvidenceManager";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Input from "@/shared/ui/Input";
import Button from "@/shared/ui/Button";
export default function AdminIssuerReviewPage() {
  const pending = useRemote(useCallback(() => governanceApi.pending(), []));
  const [org, setOrg] = useState("");
  const [searchOrg, setSearchOrg] = useState("");
  const history = useRemote(
    useCallback(
      () => (org ? governanceApi.issuers(org) : Promise.resolve([])),
      [org],
    ),
  );
  const [reason, setReason] = useState(""),
    [expiry, setExpiry] = useState("");
  const action = useAction(() => {
    pending.reload();
    history.reload();
  });
  const rows = org ? history.data : pending.data;
  return (
    <AppShell>
      <main className="sporg">
        <header className="sporg-heading">
          <h1>Issuer authorization</h1>
          <p>
            Review evidence before authorizing certificate issuance. Suspending
            an issuer does not silently revoke previously issued certificates.
          </p>
        </header>
        <form
          className="sporg-toolbar"
          onSubmit={(e) => {
            e.preventDefault();
            setOrg(searchOrg.trim());
          }}
        >
          <Input
            label="Organization UUID (optional history lookup)"
            value={searchOrg}
            onChange={(e) => setSearchOrg(e.target.value)}
          />
          <Button type="submit">Load history</Button>
          <Button variant="outline" onClick={() => setOrg("")}>
            Pending queue
          </Button>
        </form>
        <RemoteState
          {...(org ? history : pending)}
          retry={org ? history.reload : pending.reload}
        />
        <ActionNotice {...action} />
        {rows?.map((a) => (
          <section className="sporg-card" key={a.id}>
            <h2>
              {a.organizationId} · {a.status}
            </h2>
            <p>{a.scope}</p>
            <p>
              Scope: {a.scopeMode} · {a.courseVersionIds || "All courses"}
            </p>
            <EvidenceManager
              organizationId={a.organizationId}
              purpose="ISSUER"
              admin
            />
            <Input
              label="Decision reason"
              required
              maxLength={1000}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
            />
            <Input
              label="Authorization expiry"
              type="datetime-local"
              value={expiry}
              onChange={(e) => setExpiry(e.target.value)}
            />
            <div className="sporg-actions">
              {(a.status === "PENDING"
                ? ["AUTHORIZED", "REJECTED"]
                : a.status === "AUTHORIZED"
                  ? ["SUSPENDED", "REVOKED"]
                  : []
              ).map((status) => (
                <Button
                  key={status}
                  disabled={
                    action.busy ||
                    !reason.trim() ||
                    (status === "AUTHORIZED" && !expiry)
                  }
                  onClick={() => {
                    if (window.confirm(`Confirm ${status.toLowerCase()}?`))
                      void action.run(
                        () =>
                          governanceApi.decide(a.id, {
                            status,
                            reason,
                            validUntil:
                              status === "AUTHORIZED"
                                ? new Date(expiry).toISOString()
                                : null,
                          }),
                        "Issuer decision recorded.",
                      );
                  }}
                >
                  {status.toLowerCase()}
                </Button>
              ))}
            </div>
          </section>
        ))}
      </main>
    </AppShell>
  );
}
