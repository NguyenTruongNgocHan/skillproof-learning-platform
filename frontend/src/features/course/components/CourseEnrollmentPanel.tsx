import WorkspaceDialog from "@/features/organization/components/WorkspaceDialog";
import { useCallback, useState } from "react";
import { workspaceCourseApi } from "../api/workspaceCourseApi";
import { useRemote } from "@/shared/hooks/useRemote";
import { RemoteState } from "@/shared/ui/RemoteState";
import { exportPagedCsv } from "@/shared/utils/exportPagedCsv";
import { useAction } from "@/shared/hooks/useRemote";
import { ActionNotice } from "@/shared/ui/RemoteState";
import { exportCsv } from "@/shared/utils/exportCsv";
import Input from "@/shared/ui/Input";
import Button from "@/shared/ui/Button";
export function CourseEnrollmentPanel({ versionId }: { versionId: string }) {
  const [selected, setSelected] = useState<string | null>(null);
  const [query, setQuery] = useState(""),
    [search, setSearch] = useState(""),
    [page, setPage] = useState(0),
    [status, setStatus] = useState(""),
    [start, setStart] = useState(""),
    [end, setEnd] = useState("");
  const action = useAction();
  const rangeStart = start
    ? new Date(start + "T00:00:00Z").toISOString()
    : "1970-01-01T00:00:00Z";
  const rangeEnd = end
    ? new Date(new Date(end + "T00:00:00Z").getTime() + 86400000).toISOString()
    : "9999-12-31T00:00:00Z";
  const remote = useRemote(
    useCallback(
      () =>
        workspaceCourseApi.enrollments(
          versionId,
          search,
          page,
          status,
          rangeStart,
          rangeEnd,
        ),
      [versionId, search, page, status, rangeStart, rangeEnd],
    ),
  );
  return (
    <section className="sporg-card">
      <h2>Enrolled learners</h2>
      <form
        className="sporg-toolbar"
        onSubmit={(e) => {
          e.preventDefault();
          setSearch(query);
          setPage(0);
        }}
      >
        <Input
          label="Email / learner UUID"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <label>
          Status
          <select
            value={status}
            onChange={(e) => {
              setStatus(e.target.value);
              setPage(0);
            }}
          >
            <option value="">All</option>
            <option value="ACTIVE">Learning</option>
            <option value="COMPLETED">Completed</option>
          </select>
        </label>
        <Input
          label="Enrolled from (UTC)"
          type="date"
          value={start}
          onChange={(e) => {
            setStart(e.target.value);
            setPage(0);
          }}
        />
        <Input
          label="Through (UTC)"
          type="date"
          min={start}
          value={end}
          onChange={(e) => {
            setEnd(e.target.value);
            setPage(0);
          }}
        />
        <Button type="submit">Search</Button>
        <Button
          variant="outline"
          disabled={action.busy || remote.loading}
          onClick={() =>
            void action.run(
              () =>
                exportPagedCsv(
                  "enrollments-filtered.csv",
                  (p) =>
                    workspaceCourseApi.enrollments(
                      versionId,
                      search,
                      p,
                      status,
                      rangeStart,
                      rangeEnd,
                    ),
                  (r) => ({ ...r }),
                ),
              "Filtered enrollments exported.",
            )
          }
        >
          Export all filtered
        </Button>
        <Button
          variant="outline"
          disabled={!remote.data?.content.length}
          onClick={() =>
            exportCsv(
              "enrollments-page.csv",
              remote.data!.content.map((r) => ({ ...r })),
            )
          }
        >
          Export current page
        </Button>
      </form>
      <ActionNotice {...action} />
      <RemoteState {...remote} retry={remote.reload} />
      <div className="sporg-table-scroll">
        <table>
          <thead>
            <tr>
              <th>Learner</th>
              <th>Registered</th>
              <th>Status</th>
              <th>Required resource progress</th>
              <th>Details</th>
            </tr>
          </thead>
          <tbody>
            {remote.data?.content.map((r) => (
              <tr key={r.id}>
                <td>
                  {r.email}
                  <small>{r.learnerId}</small>
                </td>
                <td>{new Date(r.enrolledAt).toLocaleString()}</td>
                <td>{r.status}</td>
                <td>
                  {r.completedResources}/{r.totalResources}
                </td>
                <td>
                  <Button variant="outline" onClick={() => setSelected(r.id)}>
                    View progress
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {remote.data?.content.length === 0 && <p>No enrollments found.</p>}
      <div className="sporg-actions">
        <Button disabled={page === 0} onClick={() => setPage(page - 1)}>
          Previous
        </Button>
        <span>{remote.data?.totalElements ?? "—"} learners</span>
        <Button
          disabled={!remote.data || page + 1 >= remote.data.totalPages}
          onClick={() => setPage(page + 1)}
        >
          Next
        </Button>
      </div>
      {selected && (
        <WorkspaceDialog
          title="Learner progress"
          close={() => setSelected(null)}
        >
          <EnrollmentDetailView version={versionId} id={selected} />
        </WorkspaceDialog>
      )}
    </section>
  );
}

function EnrollmentDetailView({
  version,
  id,
}: {
  version: string;
  id: string;
}) {
  const remote = useRemote(
    useCallback(
      () => workspaceCourseApi.enrollment(version, id),
      [version, id],
    ),
  );
  return (
    <>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data && (
        <>
          <h3>{remote.data.email}</h3>
          <p>
            {remote.data.status} · Registered{" "}
            {new Date(remote.data.enrolledAt).toLocaleString()}
          </p>
          <p>
            Official assessments passed:{" "}
            {remote.data.assessments.passedOfficialAssessments}/
            {remote.data.assessments.requiredOfficialAssessments}
          </p>
          <p>
            Required assignments passed: {remote.data.assignments.passed}/
            {remote.data.assignments.required}
          </p>
          <section className="sporg-card">
            <h3>Assessment attempts</h3>
            {remote.data.assessmentHistory.length === 0 && (
              <p>No attempts recorded.</p>
            )}
            {remote.data.assessmentHistory.map((t) => (
              <div className="sporg-evidence-row" key={t.id}>
                <strong>
                  {t.title} · {t.kind} · {t.required ? "Required" : "Optional"}
                </strong>
                <p>
                  {t.status} ·{" "}
                  {t.scorePercent === null
                    ? "Not scored"
                    : `${t.scorePercent}%`}{" "}
                  ·
                  {t.passed === null
                    ? "Pending"
                    : t.passed
                      ? "Passed"
                      : "Not passed"}
                </p>
                <small>
                  Started {new Date(t.startedAt).toLocaleString()}
                  {t.submittedAt &&
                    ` · Finalized ${new Date(t.submittedAt).toLocaleString()}`}
                </small>
              </div>
            ))}
          </section>
          <section className="sporg-card">
            <h3>Assignment outcomes</h3>
            {remote.data.assignmentHistory.length === 0 && (
              <p>No assignments in this version.</p>
            )}
            {remote.data.assignmentHistory.map((a) => (
              <div className="sporg-evidence-row" key={a.id}>
                <strong>
                  {a.title} · {a.required ? "Required" : "Optional"} · Pass{" "}
                  {a.passPercent}%
                </strong>
                {a.submissions.length === 0 && (
                  <p>No submitted work yet. Learner drafts are private.</p>
                )}
                {a.submissions.map((s) => (
                  <div key={s.id}>
                    <p>
                      {s.status} ·{" "}
                      {s.scorePercent === null
                        ? "Awaiting grading"
                        : `${s.scorePercent}%`}{" "}
                      ·
                      {s.passed === null
                        ? "Pending"
                        : s.passed
                          ? "Passed"
                          : "Not passed"}
                    </p>
                    {s.submittedAt && (
                      <small>
                        Submitted {new Date(s.submittedAt).toLocaleString()}
                      </small>
                    )}
                    {s.feedback && <p>{s.feedback}</p>}
                    {s.criterionGrades.map((g) => (
                      <p key={g.criterionId}>
                        {g.title} · {g.score}% · {g.feedback}
                      </p>
                    ))}
                  </div>
                ))}
              </div>
            ))}
          </section>
          <section className="sporg-card">
            <h3>Access provenance</h3>
            {remote.data.retainedFreeAccess && (
              <p>Free learning access retained at enrollment.</p>
            )}
            {remote.data.accessHistory.length === 0 &&
              !remote.data.retainedFreeAccess && (
                <p>
                  No historical grant found. Current free-access eligibility
                  follows the effective offer.
                </p>
              )}
            {remote.data.accessHistory.map((g) => (
              <p key={g.id}>
                {g.type} · {g.source}
                {g.cohort && ` · ${g.cohort}`} ·{" "}
                {g.revokedAt
                  ? `Revoked ${new Date(g.revokedAt).toLocaleString()}`
                  : g.expiresAt
                    ? `Expires ${new Date(g.expiresAt).toLocaleString()}`
                    : "No expiry"}
              </p>
            ))}
          </section>
          <section className="sporg-card">
            <h3>Completion and certificates</h3>
            <p>
              {remote.data.completedAt
                ? `Completed ${new Date(remote.data.completedAt).toLocaleString()}`
                : "Course not yet completed"}
            </p>
            {remote.data.certificates.length === 0 ? (
              <p>No certificate issued for this version.</p>
            ) : (
              remote.data.certificates.map((c) => (
                <p key={c.id}>
                  {c.program} · {c.serial} · {c.status} ·{" "}
                  {new Date(c.issuedAt).toLocaleString()}
                  {c.reason && ` · ${c.reason}`}
                </p>
              ))
            )}
            {remote.data.completionEvidenceJson && (
              <details>
                <summary>Latest evaluated evidence</summary>
                <pre className="sporg-evidence-json">
                  {JSON.stringify(
                    JSON.parse(remote.data.completionEvidenceJson),
                    null,
                    2,
                  )}
                </pre>
              </details>
            )}
          </section>
          {remote.data.outline.map((m, i) => (
            <section className="sporg-card" key={i}>
              <h3>{m.title}</h3>
              {m.lessons.map((l) => (
                <p key={l.id}>
                  {l.completed ? "✓" : "○"} {l.title}
                </p>
              ))}
              {m.resources.map((r) => (
                <p key={r.id}>
                  {r.completedAt ? "✓" : "○"} {r.title} ·{" "}
                  {r.required ? "Required" : "Optional"}
                  {r.completedAt &&
                    ` · ${new Date(r.completedAt).toLocaleString()}`}
                </p>
              ))}
            </section>
          ))}
        </>
      )}
    </>
  );
}
