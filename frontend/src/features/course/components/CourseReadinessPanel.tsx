import { useCallback } from "react";
import { workspaceCourseApi } from "../api/workspaceCourseApi";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import { courseApi } from "../api/courseApi";
import Button from "@/shared/ui/Button";
export function CourseReadinessPanel({
  versionId,
  navigate,
  published,
}: {
  versionId: string;
  navigate: (tab: string, targetId?: string) => void;
  published: () => void;
}) {
  const remote = useRemote(
    useCallback(() => workspaceCourseApi.readiness(versionId), [versionId]),
  );
  const action = useAction(published);
  return (
    <section className="sporg-card">
      <h2>Publish checklist</h2>
      <p>
        Review your learner preview, completion rules and offer before
        publishing. Published content cannot be edited.
      </p>
      <RemoteState {...remote} retry={remote.reload} />
      <ActionNotice {...action} />
      {remote.data?.issues.map((i) => (
        <button
          className="sporg-checklist-issue"
          key={i.code + i.targetId}
          onClick={() => navigate(i.tab, i.targetId)}
        >
          <strong>{i.code}</strong> {i.message} →
        </button>
      ))}
      {remote.data?.ready && (
        <p className="sporg-success">
          Content checks are ready. Backend will recheck under transaction locks
          when publishing.
        </p>
      )}
      <div className="sporg-actions">
        <Button variant="outline" onClick={() => navigate("preview")}>
          Review learner preview
        </Button>
        <Button variant="outline" onClick={remote.reload}>
          Recheck
        </Button>
        <Button
          disabled={
            !remote.data?.ready || remote.data.status !== "DRAFT" || action.busy
          }
          onClick={() => {
            if (window.confirm("Publish this immutable version?"))
              void action.run(
                () => courseApi.publish(versionId),
                "Version published.",
              );
          }}
        >
          Publish version
        </Button>
      </div>
    </section>
  );
}
