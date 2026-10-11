import { ReviewModerationHistory } from "../components/ReviewModerationHistory";
import { exportPagedCsv } from "@/shared/utils/exportPagedCsv";
import { useCallback, useState } from "react";
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import { exportCsv } from "@/shared/utils/exportCsv";
import { feedbackApi } from "../api/feedbackApi";
import Button from "@/shared/ui/Button";
export default function OrganizationFeedbackPage() {
  const { organization } = useOrganizationContext();
  const org = organization?.id ?? "";
  const [page, setPage] = useState(0),
    [replies, setReplies] = useState<Record<string, string>>({});
  const remote = useRemote(
    useCallback(() => feedbackApi.organization(org, page), [org, page]),
  );
  const action = useAction(remote.reload);
  return (
    <OrganizerSurface
      title="Learner feedback"
      description="Read verified feedback, respond professionally and report abusive content."
      authority="MANAGE_FEEDBACK"
    >
      <Button
        variant="outline"
        disabled={!remote.data?.content.length}
        onClick={() =>
          exportCsv(
            "feedback-page.csv",
            remote.data!.content.map((r) => ({ ...r })),
          )
        }
      >
        Export current page
      </Button>
      <Button
        variant="outline"
        disabled={action.busy}
        onClick={() =>
          void action.run(
            () =>
              exportPagedCsv(
                "feedback-all.csv",
                (p) => feedbackApi.organization(org, p),
                (r) => ({ ...r }),
              ),
            "Feedback exported.",
          )
        }
      >
        Export all reviews
      </Button>
      <RemoteState {...remote} retry={remote.reload} />
      <ActionNotice {...action} />
      {remote.data?.content.length === 0 && (
        <div className="sporg-empty">No feedback yet.</div>
      )}
      {remote.data?.content.map((r) => (
        <section className="sporg-card" key={r.id}>
          <span className="sporg-badge">
            {r.type} · {r.rating}/5 · {r.status}
          </span>
          <p>{r.body}</p>
          <small>
            Target {r.targetId} · {new Date(r.createdAt).toLocaleString()}
          </small>
          {r.reply && <blockquote>{r.reply}</blockquote>}
          <ReviewModerationHistory id={r.id} />
          <form
            className="sporg-form"
            onSubmit={(e) => {
              e.preventDefault();
              void action.run(
                () => feedbackApi.reply(r.id, replies[r.id] ?? ""),
                "Response saved.",
              );
            }}
          >
            <label>
              Your response
              <textarea
                required
                maxLength={2000}
                value={replies[r.id] ?? r.reply ?? ""}
                onChange={(e) =>
                  setReplies({ ...replies, [r.id]: e.target.value })
                }
              />
            </label>
            <div className="sporg-actions">
              <Button disabled={action.busy} type="submit">
                Respond
              </Button>
              <Button
                variant="outline"
                disabled={action.busy}
                onClick={() => {
                  const reason = window.prompt(
                    "Why are you reporting this review?",
                  );
                  if (reason?.trim())
                    void action.run(
                      () => feedbackApi.report(r.id, reason),
                      "Report submitted to moderation.",
                    );
                }}
              >
                Report
              </Button>
            </div>
          </form>
        </section>
      ))}
      {remote.data && (
        <div className="sporg-actions">
          <Button
            variant="outline"
            disabled={page === 0}
            onClick={() => setPage(page - 1)}
          >
            Previous
          </Button>
          <span>
            Page {page + 1} · {remote.data.totalElements} reviews
          </span>
          <Button
            variant="outline"
            disabled={page + 1 >= remote.data.totalPages}
            onClick={() => setPage(page + 1)}
          >
            Next
          </Button>
        </div>
      )}
    </OrganizerSurface>
  );
}
