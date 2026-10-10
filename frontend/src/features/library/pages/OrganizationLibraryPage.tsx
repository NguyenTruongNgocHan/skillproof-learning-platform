import { useCallback, useState } from "react";
import { FileText, Plus } from "lucide-react";
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { useAction, useRemote } from "@/shared/hooks/useRemote";
import { ActionNotice, RemoteState } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import WorkspaceDialog from "@/features/organization/components/WorkspaceDialog";
import { libraryApi } from "../api/libraryApi";
import type { LibraryResource } from "../types/library.types";
import { LibraryEditor } from "../components/LibraryEditor";
export default function OrganizationLibraryPage() {
  const [editorBusy, setEditorBusy] = useState(false);
  const { organization, grants } = useOrganizationContext();
  const org = organization?.id ?? "";
  const remote = useRemote(
    useCallback(
      () =>
        grants.includes("MANAGE_CONTENT")
          ? libraryApi.list(org)
          : Promise.resolve([]),
      [org, grants],
    ),
  );
  const action = useAction(remote.reload);
  const [search, setSearch] = useState(""),
    [create, setCreate] = useState(false),
    [selected, setSelected] = useState<LibraryResource | null>(null);
  return (
    <OrganizerSurface
      title="Resource library"
      description="Share articles and files, with free, paid or restricted access."
      authority="MANAGE_CONTENT"
    >
      <div className="sporg-toolbar">
        <input
          aria-label="Search resources"
          placeholder="Search resources…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <Button
          onClick={() => {
            setSelected(null);
            setCreate(true);
          }}
        >
          <Plus size={16} /> Add resource
        </Button>
      </div>
      <RemoteState {...remote} retry={remote.reload} />
      <ActionNotice {...action} />
      {remote.data?.length === 0 && (
        <section className="sporg-empty">
          <FileText size={32} />
          <h2>Your resource library</h2>
          <p>
            Create an article or attach learning files, then submit for review.
          </p>
        </section>
      )}
      {remote.data
        ?.filter((r) =>
          (r.title + " " + r.summary)
            .toLowerCase()
            .includes(search.toLowerCase()),
        )
        .map((r) => (
          <section className="sporg-card sporg-assignment-row" key={r.id}>
            <div>
              <span
                className={`sporg-badge sporg-badge-${r.status.toLowerCase()}`}
              >
                {r.status.toLowerCase()}
              </span>
              <h3>{r.title}</h3>
              <p>{r.summary}</p>
              <small>
                {r.accessMode === "RESTRICTED"
                  ? "Restricted"
                  : r.priceVnd === 0
                    ? "Free"
                    : new Intl.NumberFormat("vi-VN", {
                        style: "currency",
                        currency: "VND",
                      }).format(r.priceVnd)}
              </small>
              {r.reviewReason && <p>Review: {r.reviewReason}</p>}
            </div>
            <div className="sporg-actions">
              <Button
                variant="outline"
                onClick={() => {
                  setCreate(false);
                  setSelected(r);
                }}
              >
                Open
              </Button>
              {r.status === "DRAFT" && (
                <Button
                  disabled={action.busy}
                  onClick={() => {
                    if (
                      window.confirm(
                        "Submit this resource for administrator review?",
                      )
                    )
                      void action.run(
                        () => libraryApi.submit(r.id),
                        "Resource submitted for review.",
                      );
                  }}
                >
                  Submit for review
                </Button>
              )}
            </div>
          </section>
        ))}
      {(create || selected) && (
        <WorkspaceDialog
          busy={editorBusy}
          title={selected?.title ?? "Create library resource"}
          close={() => {
            if (window.confirm("Close this resource editor?")) {
              setCreate(false);
              setSelected(null);
            }
          }}
        >
          <LibraryEditor
            onBusyChange={setEditorBusy}
            org={org}
            value={selected}
            created={(r) => {
              setCreate(false);
              setSelected(r);
              remote.reload();
            }}
            refresh={remote.reload}
          />
        </WorkspaceDialog>
      )}
    </OrganizerSurface>
  );
}
