import { useState, useCallback } from "react";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface";
import WorkspaceDialog from "@/features/organization/components/WorkspaceDialog";
import { apiClient } from "@/shared/api/apiClient";
import { useRemote } from "@/shared/hooks/useRemote";
import { RemoteState } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import type { Assignment, Submission } from "../types/assignment.types";
import { AssignmentSubmissions } from "../components/AssignmentSubmissions";
export default function OrganizationGradingPage() {
  const { organization } = useOrganizationContext();
  const [queuePage, setQueuePage] = useState(0);
  const [initialSubmission, setInitialSubmission] = useState<
      Submission | undefined
    >(),
    [status, setStatus] = useState("SUBMITTED");
  const queue = useRemote(
    useCallback(
      () =>
        apiClient.get<{
          content: {
            assignment: Assignment;
            submission: Submission;
            email: string;
          }[];
          totalPages: number;
          totalElements: number;
        }>(
          `/organizations/${organization?.id}/grading/queue?status=${status}&page=${queuePage}`,
        ),
      [organization?.id, status, queuePage],
    ),
  );
  const [page, setPage] = useState(0),
    [selected, setSelected] = useState<Assignment | null>(null),
    [busy, setBusy] = useState(false);
  const remote = useRemote(
    useCallback(
      () =>
        apiClient.get<{ content: Assignment[]; totalPages: number }>(
          `/organizations/${organization?.id}/grading?page=${page}`,
        ),
      [organization?.id, page],
    ),
  );
  return (
    <OrganizerSurface
      title="Assignment grading"
      description="Review submissions for published and historical course versions. Grades are final and completion is reevaluated after grading."
      authority="GRADE_ASSIGNMENTS"
    >
      <section className="sporg-card">
        <h2>Grading queue</h2>
        <label>
          Submission status
          <select
            value={status}
            onChange={(e) => {
              setStatus(e.target.value);
              setQueuePage(0);
            }}
          >
            <option value="SUBMITTED">Awaiting grade</option>
            <option value="GRADED">Graded</option>
          </select>
        </label>
        <RemoteState {...queue} retry={queue.reload} />
        <p>{queue.data?.totalElements ?? 0} submissions</p>
        {queue.data?.content.map((row) => (
          <div key={row.submission.id} className="sporg-row">
            <div>
              <strong>{row.assignment.title}</strong>
              <small>
                {row.email} ·{" "}
                {row.submission.submittedAt &&
                  new Date(row.submission.submittedAt).toLocaleString()}
              </small>
            </div>
            <Button
              onClick={() => {
                setInitialSubmission(row.submission);
                setSelected(row.assignment);
              }}
            >
              Review submission
            </Button>
          </div>
        ))}
        {queue.data?.content.length === 0 && (
          <p>No submissions in this queue.</p>
        )}
        <div className="sporg-actions">
          <Button
            variant="outline"
            disabled={queuePage === 0 || queue.loading}
            onClick={() => setQueuePage(queuePage - 1)}
          >
            Previous queue page
          </Button>
          <Button
            variant="outline"
            disabled={
              !queue.data ||
              queuePage + 1 >= queue.data.totalPages ||
              queue.loading
            }
            onClick={() => setQueuePage(queuePage + 1)}
          >
            Next queue page
          </Button>
        </div>
      </section>
      <h2>Browse assignments</h2>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data?.content.length === 0 && (
        <section className="sporg-card">No published assignments.</section>
      )}
      {remote.data?.content.map((a) => (
        <section className="sporg-card" key={a.id}>
          <h2>{a.title}</h2>
          <p>{a.instructions}</p>
          <p>
            Pass {a.passPercent}% · {a.required ? "Required" : "Optional"}
          </p>
          <Button
            onClick={() => {
              setInitialSubmission(undefined);
              setSelected(a);
            }}
          >
            Review submissions
          </Button>
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
      {selected && (
        <WorkspaceDialog
          title={selected.title}
          busy={busy}
          close={() => {
            setSelected(null);
            queue.reload();
          }}
        >
          <AssignmentSubmissions
            assignment={selected}
            initialSubmission={initialSubmission}
            onBusyChange={setBusy}
          />
        </WorkspaceDialog>
      )}
    </OrganizerSurface>
  );
}
