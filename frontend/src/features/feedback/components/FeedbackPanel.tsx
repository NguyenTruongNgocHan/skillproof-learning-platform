import { useCallback, useState } from "react";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import { feedbackApi } from "../api/feedbackApi";
import { FeedbackForm } from "./FeedbackForm";
import Button from "@/shared/ui/Button";
export function FeedbackPanel({
  type,
  target,
}: {
  type: "COURSE" | "RESOURCE" | "CERTIFICATE";
  target: string;
}) {
  const { user } = useAuth();
  const [page, setPage] = useState(0);
  const remote = useRemote(
    useCallback(
      () => feedbackApi.publicList(type, target, page),
      [type, target, page],
    ),
  );
  const action = useAction();
  return (
    <section className="sporg-card">
      <h2>Verified learner feedback</h2>
      <RemoteState {...remote} retry={remote.reload} />
      <ActionNotice {...action} />
      {remote.data && (
        <>
          <p>
            {remote.data.count} reviews ·{" "}
            {remote.data.average == null
              ? "No rating yet"
              : `${remote.data.average.toFixed(1)} / 5`}
          </p>
          {remote.data.reviews.content.map((r) => (
            <article className="sporg-evidence-row" key={r.id}>
              <strong>{r.rating}/5 · Verified experience</strong>
              <p>{r.body}</p>
              {r.reply && (
                <blockquote>Organization response: {r.reply}</blockquote>
              )}
              <small>{new Date(r.createdAt).toLocaleDateString()}</small>
              {user && (
                <Button
                  variant="ghost"
                  onClick={() => {
                    const reason = window.prompt(
                      "Explain the policy violation (negative feedback alone is not a violation)",
                    );
                    if (reason?.trim())
                      void action.run(
                        () => feedbackApi.report(r.id, reason),
                        "Report sent for moderation.",
                      );
                  }}
                >
                  Report
                </Button>
              )}
            </article>
          ))}
          <div className="sporg-actions">
            <Button
              variant="outline"
              disabled={page === 0}
              onClick={() => setPage(page - 1)}
            >
              Previous
            </Button>
            <Button
              variant="outline"
              disabled={page + 1 >= remote.data.reviews.totalPages}
              onClick={() => setPage(page + 1)}
            >
              Next
            </Button>
            <Button variant="outline" onClick={remote.reload}>
              Refresh reviews
            </Button>
          </div>
        </>
      )}
      {user?.role === "LEARNER" ? (
        <FeedbackForm type={type} target={target} />
      ) : (
        <p>Sign in as a learner to share verified feedback.</p>
      )}
    </section>
  );
}
