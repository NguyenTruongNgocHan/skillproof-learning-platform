import { useCallback, useState } from "react";
import AppShell from "@/shared/components/layout/AppShell";
import MediaPanel from "@/features/media/components/MediaPanel";
import { libraryApi } from "@/features/library/api/libraryApi";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Input from "@/shared/ui/Input";
import Button from "@/shared/ui/Button";
export default function AdminLibraryReviewPage() {
  const [page, setPage] = useState(0),
    [selected, setSelected] = useState(""),
    [reason, setReason] = useState("");
  const queue = useRemote(
    useCallback(() => libraryApi.reviewQueue(page), [page]),
  );
  const detail = useRemote(
    useCallback(
      async () => (selected ? libraryApi.reviewDetail(selected) : null),
      [selected],
    ),
  );
  const action = useAction(() => {
    queue.reload();
    detail.reload();
  });
  return (
    <AppShell>
      <main className="sporg">
        <header className="sporg-heading">
          <h1>Resource review</h1>
          <p>
            Read the actual learning material and inspect uploaded files before
            deciding.
          </p>
        </header>
        <RemoteState {...queue} retry={queue.reload} />
        <ActionNotice {...action} />
        <div className="sporg-grid">
          <section className="sporg-card">
            <h2>Pending submissions</h2>
            {queue.data?.length === 0 && <p>No pending resources.</p>}
            {queue.data?.map((r) => (
              <button
                className="sporg-checklist-issue"
                key={r.id}
                onClick={() => {
                  setSelected(r.id);
                  setReason("");
                }}
              >
                {r.title}
              </button>
            ))}
            <div className="sporg-actions">
              <Button disabled={page === 0} onClick={() => setPage(page - 1)}>
                Previous
              </Button>
              <Button
                disabled={!queue.data || queue.data.length < 20}
                onClick={() => setPage(page + 1)}
              >
                Next
              </Button>
            </div>
          </section>
          <section className="sporg-card">
            <RemoteState {...detail} retry={detail.reload} />
            {detail.data ? (
              <>
                <h2>{detail.data.title}</h2>
                <p>{detail.data.summary}</p>
                <p className="sporg-lesson-body">{detail.data.body}</p>
                <p>
                  {detail.data.priceVnd.toLocaleString("vi-VN")} VND ·{" "}
                  {detail.data.accessMode}
                </p>
                <MediaPanel scope="LIBRARY" target={detail.data.id} />
                <Input
                  label="Review reason"
                  required
                  maxLength={1000}
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                />
                <div className="sporg-actions">
                  <Button
                    disabled={action.busy || !reason.trim()}
                    onClick={() =>
                      void action.run(
                        () => libraryApi.review(selected, true, reason),
                        "Resource published.",
                      )
                    }
                  >
                    Approve
                  </Button>
                  <Button
                    variant="destructive"
                    disabled={action.busy || !reason.trim()}
                    onClick={() =>
                      void action.run(
                        () => libraryApi.review(selected, false, reason),
                        "Resource returned with reason.",
                      )
                    }
                  >
                    Reject
                  </Button>
                </div>
              </>
            ) : (
              <p>Select a submitted resource.</p>
            )}
          </section>
        </div>
      </main>
    </AppShell>
  );
}
