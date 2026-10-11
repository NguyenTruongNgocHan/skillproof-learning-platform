import { ReviewModerationHistory } from "@/features/feedback/components/ReviewModerationHistory";
import { useCallback, useState } from "react";
import AppShell from "@/shared/components/layout/AppShell";
import { feedbackApi } from "@/features/feedback/api/feedbackApi";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Input from "@/shared/ui/Input";
import Button from "@/shared/ui/Button";
export default function AdminFeedbackModerationPage() {
  const [page, setPage] = useState(0),
    [reason, setReason] = useState("");
  const remote = useRemote(
    useCallback(() => feedbackApi.reports(page), [page]),
  );
  const action = useAction(remote.reload);
  return (
    <AppShell>
      <main className="sporg">
        <header className="sporg-heading">
          <h1>Feedback moderation</h1>
          <p>
            Negative feedback alone is not a reason to hide a review. Record a
            specific policy reason.
          </p>
        </header>
        <RemoteState {...remote} retry={remote.reload} />
        <ActionNotice {...action} />
        <Input
          label="Resolution reason"
          maxLength={1000}
          value={reason}
          onChange={(e) => setReason(e.target.value)}
        />
        {remote.data?.content.map((r) => (
          <section className="sporg-card" key={r.id}>
            <h2>Review {r.reviewId}</h2>
            <p>Report: {r.reason}</p>
            <ReviewDetail id={r.reviewId} />
            <div className="sporg-actions">
              <Button
                disabled={action.busy || !reason.trim()}
                onClick={() =>
                  void action.run(
                    () => feedbackApi.resolve(r.id, false, reason),
                    "Report resolved; review retained.",
                  )
                }
              >
                Keep review
              </Button>
              <Button
                variant="destructive"
                disabled={action.busy || !reason.trim()}
                onClick={() =>
                  void action.run(
                    () => feedbackApi.resolve(r.id, true, reason),
                    "Review hidden with audit reason.",
                  )
                }
              >
                Hide review
              </Button>
            </div>
          </section>
        ))}
        <div className="sporg-actions">
          <Button disabled={page === 0} onClick={() => setPage(page - 1)}>
            Previous
          </Button>
          <Button
            disabled={!remote.data || page + 1 >= remote.data.totalPages}
            onClick={() => setPage(page + 1)}
          >
            Next
          </Button>
        </div>
      </main>
    </AppShell>
  );
}

function ReviewDetail({ id }: { id: string }) {
  const remote = useRemote(
    useCallback(() => feedbackApi.adminReview(id), [id]),
  );
  return (
    <>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data && (
        <blockquote>
          <strong>
            {remote.data.rating}/5 · {remote.data.type}
          </strong>
          <p>{remote.data.body}</p>
          <p>{remote.data.reply}</p>
          <ReviewModerationHistory id={id} />
        </blockquote>
      )}
    </>
  );
}
