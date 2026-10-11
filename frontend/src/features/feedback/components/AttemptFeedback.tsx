import { useCallback } from "react";
import { apiClient } from "@/shared/api/apiClient";
import { useRemote } from "@/shared/hooks/useRemote";
import { RemoteState } from "@/shared/ui/RemoteState";
import { FeedbackForm } from "./FeedbackForm";
export function AttemptFeedback({
  attemptId,
  assessmentId,
}: {
  attemptId: string;
  assessmentId: string;
}) {
  const remote = useRemote(
    useCallback(
      () =>
        apiClient.get<{ id: string; stem: string; position: number }[]>(
          `/quiz/attempts/${attemptId}/feedback-questions`,
        ),
      [attemptId],
    ),
  );
  return (
    <section className="sporg">
      <details>
        <summary>Give private assessment feedback</summary>
        <FeedbackForm type="ASSESSMENT" target={assessmentId} />
      </details>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data?.map((q) => (
        <details key={q.id}>
          <summary>
            Question {q.position}: {q.stem}
          </summary>
          <FeedbackForm type="QUESTION_VERSION" target={q.id} />
        </details>
      ))}
    </section>
  );
}
