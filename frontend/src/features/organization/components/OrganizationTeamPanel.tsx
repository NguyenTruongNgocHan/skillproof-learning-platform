import { organizationApi } from "@/features/organization/api/organizationApi";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import type {
  Authority,
  Invitation,
  Member,
} from "@/features/organization/types/organization.types";
import { organizerError } from "@/features/organization/utils/errorMessage";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
import { useToast } from "@/shared/ui/Toast";
import { useEffect, useState } from "react";
const authorities: Authority[] = [
  "MANAGE_PROFILE",
  "MANAGE_MEMBERS",
  "MANAGE_CONTENT",
  "ISSUE_CERTIFICATES",
];
const labels: Record<Authority, string> = {
  MANAGE_PROFILE: "Manage profile",
  MANAGE_MEMBERS: "Manage team",
  MANAGE_CONTENT: "Author content",
  ISSUE_CERTIFICATES: "Issue certificates",
};
export function OrganizationTeamPanel({
  onDirtyChange,
}: { onDirtyChange?: (dirty: boolean) => void } = {}) {
  const { organization, refresh, setDirty } = useOrganizationContext();
  const { toast } = useToast();
  const [members, setMembers] = useState<Member[]>([]),
    [invitations, setInvitations] = useState<Invitation[]>([]),
    [email, setEmail] = useState(""),
    [query, setQuery] = useState(""),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false),
    [loading, setLoading] = useState(true),
    [reload, setReload] = useState(0);
  useEffect(() => {
    onDirtyChange?.(Boolean(email));
    return () => onDirtyChange?.(false);
  }, [email, onDirtyChange]);
  useEffect(() => {
    setDirty("invitation", Boolean(email));
    return () => setDirty("invitation", false);
  }, [email, setDirty]);
  useEffect(() => {
    let live = true;
    if (!organization) return;
    setLoading(true);
    Promise.all([
      organizationApi.members(organization.id),
      organizationApi.invitations(organization.id),
    ])
      .then(([m, i]) => {
        if (live) {
          setMembers(m);
          setInvitations(i);
          setError("");
        }
      })
      .catch((e) => {
        if (live) setError(e.message);
      })
      .finally(() => {
        if (live) setLoading(false);
      });
    return () => {
      live = false;
    };
  }, [organization, reload]);
  async function run(
    action: () => Promise<unknown>,
    message: string,
    permissions = false,
  ) {
    if (busy) return;
    setBusy(true);
    setError("");
    try {
      await action();
      setReload((x) => x + 1);
      toast("success", message);
      if (permissions) await refresh();
    } catch (e) {
      setError(organizerError(e, "Action failed"));
    } finally {
      setBusy(false);
    }
  }
  if (!organization) return null;
  return (
    <section className="sporg-card">
      <h2>Team and access</h2>
      <p>
        Invite an existing active Organizer. After acceptance, assign the
        permissions needed for their work. Owner permissions are protected.
      </p>
      {error && (
        <div role="alert" className="sporg-alert">
          {error}
          <Button variant="outline" onClick={() => setReload((x) => x + 1)}>
            Retry
          </Button>
        </div>
      )}
      <form
        className="sporg-form"
        onSubmit={(e) => {
          e.preventDefault();
          void run(async () => {
            await organizationApi.invite(organization.id, email.trim());
            setEmail("");
            setDirty("invitation", false);
          }, "Invitation sent.");
        }}
      >
        <Input
          label="Organizer email"
          type="email"
          required
          value={email}
          maxLength={320}
          disabled={busy}
          onChange={(e) => setEmail(e.target.value)}
        />
        <Button type="submit" disabled={busy || !email.trim()}>
          Send invitation
        </Button>
      </form>
      <h3>Members</h3>
      <Input
        label="Search members"
        type="search"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />
      {loading ? (
        <p role="status">Loading team…</p>
      ) : (
        members
          .filter((m) => m.email.toLowerCase().includes(query.toLowerCase()))
          .map((m) => (
            <article className="sporg-member" key={m.user_id}>
              <div className="sporg-row">
                <div>
                  <strong>{m.email}</strong>
                  <small>
                    {m.user_id === organization.ownerUserId
                      ? "Owner"
                      : m.active
                        ? "Active member"
                        : "Inactive member"}
                  </small>
                </div>
                {m.user_id !== organization.ownerUserId && m.active && (
                  <Button
                    variant="destructive"
                    disabled={busy}
                    onClick={() => {
                      if (
                        window.confirm(
                          `Remove ${m.email} from this organization?`,
                        )
                      )
                        void run(
                          () =>
                            organizationApi.removeMember(
                              organization.id,
                              m.user_id,
                            ),
                          "Member removed.",
                          true,
                        );
                    }}
                  >
                    Remove
                  </Button>
                )}
              </div>
              <div className="sporg-permissions">
                {authorities.map((a) => (
                  <label key={a}>
                    <input
                      type="checkbox"
                      checked={m.grants.includes(a)}
                      disabled={
                        busy ||
                        !m.active ||
                        m.user_id === organization.ownerUserId
                      }
                      onChange={(e) =>
                        void run(
                          () =>
                            organizationApi.grant(
                              organization.id,
                              m.user_id,
                              a,
                              e.target.checked,
                            ),
                          "Permission updated.",
                          true,
                        )
                      }
                    />
                    {labels[a]}
                  </label>
                ))}
              </div>
            </article>
          ))
      )}
      <h3>Invitations</h3>
      {!loading && !invitations.length && <p>No invitations yet.</p>}
      {invitations.map((i) => (
        <div className="sporg-row" key={i.id}>
          <div>
            <strong>{i.email}</strong>
            <small>
              {i.status} · expires {new Date(i.expiresAt).toLocaleString()}
            </small>
          </div>
          <div className="sporg-actions">
            {i.status === "PENDING" && (
              <Button
                variant="outline"
                disabled={busy}
                onClick={() => {
                  if (window.confirm(`Revoke invitation to ${i.email}?`))
                    void run(
                      () => organizationApi.revokeInvitation(i.id),
                      "Invitation revoked.",
                    );
                }}
              >
                Revoke
              </Button>
            )}
            {(i.status === "EXPIRED" || i.status === "REVOKED") && (
              <Button
                variant="outline"
                disabled={busy}
                onClick={() =>
                  void run(
                    () => organizationApi.resendInvitation(i.id),
                    "New invitation sent. The old link remains invalid.",
                  )
                }
              >
                Resend
              </Button>
            )}
          </div>
        </div>
      ))}
    </section>
  );
}
