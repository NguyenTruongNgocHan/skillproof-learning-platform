import { exportPagedCsv } from "@/shared/utils/exportPagedCsv";
import { useCallback, useState } from "react";
import { Link } from "react-router-dom";
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { operationsApi } from "../api/operationsApi";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import { exportCsv } from "@/shared/utils/exportCsv";
import Button from "@/shared/ui/Button";
export default function OrganizationActivityPage() {
  const { organization, grants } = useOrganizationContext();
  const org = organization?.id ?? "";
  const [page, setPage] = useState(0);
  const notices = useRemote(
    useCallback(() => operationsApi.notices(page), [page]),
  );
  const audit = useRemote(
    useCallback(
      () =>
        grants.includes("MANAGE_MEMBERS")
          ? operationsApi.audit(org, page)
          : Promise.resolve(null),
      [org, page, grants],
    ),
  );
  const action = useAction(notices.reload);
  return (
    <OrganizerSurface
      title="Activity & notifications"
      description="Review work requiring attention and the history of sensitive changes."
    >
      <RemoteState {...notices} retry={notices.reload} />
      <ActionNotice {...action} />
      <section className="sporg-card">
        <h2>Notifications</h2>
        {notices.data?.content.length === 0 && <p>No notifications yet.</p>}
        {notices.data?.content.map((n) => (
          <div key={n.id} className="sporg-evidence-row">
            <div>
              <strong>{n.category}</strong>
              <p>{n.message}</p>
              <small>{new Date(n.createdAt).toLocaleString()}</small>
            </div>
            <Link
              to={
                n.link.startsWith("/") && !n.link.startsWith("//")
                  ? n.link
                  : "/organizer"
              }
            >
              Open
            </Link>
            {!n.readAt && (
              <Button
                variant="outline"
                disabled={action.busy}
                onClick={() =>
                  void action.run(
                    () => operationsApi.read(n.id),
                    "Marked as read.",
                  )
                }
              >
                Mark read
              </Button>
            )}
          </div>
        ))}
      </section>
      {grants.includes("MANAGE_MEMBERS") && (
        <section className="sporg-card">
          <h2>Audit log</h2>
          <Button
            variant="outline"
            disabled={action.busy}
            onClick={() =>
              void action.run(
                () =>
                  exportPagedCsv(
                    "audit-all.csv",
                    (p) => operationsApi.audit(org, p),
                    (r) => ({ ...r }),
                  ),
                "Audit exported.",
              )
            }
          >
            Export all audit entries
          </Button>
          <Button
            variant="outline"
            disabled={!audit.data?.content.length}
            onClick={() =>
              exportCsv(
                "audit-page.csv",
                audit.data!.content.map((r) => ({ ...r })),
              )
            }
          >
            Export current page
          </Button>
          <RemoteState {...audit} retry={audit.reload} />
          {audit.data?.content.map((a) => (
            <div className="sporg-evidence-row" key={a.id}>
              <strong>{a.action}</strong>
              <p>{a.details}</p>
              <small>
                {a.actorId} · {new Date(a.createdAt).toLocaleString()}
              </small>
            </div>
          ))}
        </section>
      )}
      <div className="sporg-actions">
        <Button disabled={page === 0} onClick={() => setPage(page - 1)}>
          Previous
        </Button>
        <Button
          disabled={
            page + 1 >=
            Math.max(notices.data?.totalPages ?? 0, audit.data?.totalPages ?? 0)
          }
          onClick={() => setPage(page + 1)}
        >
          Next
        </Button>
      </div>
    </OrganizerSurface>
  );
}
