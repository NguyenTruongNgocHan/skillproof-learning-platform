import { useState, useCallback, useEffect, useRef } from "react";
import type {
  CourseOutline,
  CourseRecord,
  CourseResource,
} from "../types/course.types";
import { mediaApi } from "@/features/media/api/mediaApi";
import { useRemote } from "@/shared/hooks/useRemote";
import { RemoteState } from "@/shared/ui/RemoteState";
import { quizApi } from "@/features/quiz/api/quizApi";
import { assignmentApi } from "@/features/assignment/api/assignmentApi";
import Button from "@/shared/ui/Button";
function FilePreview({ resource }: { resource: CourseResource }) {
  const remote = useRemote(
    useCallback(() => mediaApi.list("RESOURCE", resource.id), [resource.id]),
  );
  const [urls, setUrls] = useState<Record<string, string>>({}),
    [error, setError] = useState("");
  const retained = useRef<string[]>([]);
  useEffect(
    () => () => {
      retained.current.forEach((u) => URL.revokeObjectURL(u));
    },
    [],
  );
  return (
    <>
      <RemoteState {...remote} retry={remote.reload} />
      {error && <p role="alert">{error}</p>}
      {remote.data?.length === 0 && <p>File not uploaded yet.</p>}
      {remote.data?.map((f) => (
        <div className="sporg-preview-file" key={f.id}>
          <strong>{f.name}</strong>
          <div className="sporg-actions">
            <Button
              variant="outline"
              onClick={() => {
                void mediaApi
                  .playback(f.id)
                  .then((link) => {
                    setUrls((old) => ({ ...old, [f.id]: link.url }));
                  })
                  .catch((e) => setError(e.message));
              }}
            >
              Load preview
            </Button>
            <Button
              variant="outline"
              onClick={() =>
                void mediaApi.save(f).catch((e) => setError(e.message))
              }
            >
              Download
            </Button>
          </div>
          {urls[f.id] &&
            (f.mimeType.startsWith("image/") ? (
              <img src={urls[f.id]} alt={f.name} />
            ) : f.mimeType.startsWith("video/") ? (
              <video src={urls[f.id]} controls />
            ) : f.mimeType.startsWith("audio/") ? (
              <audio src={urls[f.id]} controls />
            ) : f.mimeType === "application/pdf" ? (
              <iframe src={urls[f.id]} title={f.name} />
            ) : (
              <p>Use download to open this file.</p>
            ))}
        </div>
      ))}
    </>
  );
}
export function CoursePreview({
  course,
  outline,
}: {
  course: CourseRecord;
  outline: CourseOutline;
}) {
  const lessons = outline.modules.flatMap((m) => m.lessons);
  const [selected, setSelected] = useState(lessons[0]?.id ?? "");
  const lesson = lessons.find((l) => l.id === selected) ?? lessons[0];
  const resources = outline.modules
    .flatMap((m) => m.resources)
    .filter(
      (r) =>
        r.lesson_id === lesson?.id ||
        (r.lesson_id === null && r.module_id === lesson?.moduleId),
    );
  const assessments = useRemote(
    useCallback(
      () => quizApi.assessments(outline.version.id),
      [outline.version.id],
    ),
  );
  const assignments = useRemote(
    useCallback(
      () => assignmentApi.list(outline.version.id),
      [outline.version.id],
    ),
  );
  return (
    <>
      <section className="sporg-card">
        <span className="sporg-badge">
          AUTHOR PREVIEW · v{outline.version.version_no} ·{" "}
          {outline.version.status}
        </span>
        <h2>{outline.version.title ?? course.title}</h2>
        <p>{outline.version.summary ?? course.summary}</p>
        <p>
          This preview does not enroll you, record progress, submit attempts or
          count marketing views. Course completion and certification are
          separate.
        </p>
      </section>
      <div className="sporg-studio-layout">
        <aside className="sporg-outline-nav" aria-label="Course contents">
          {outline.modules.map((m) => (
            <section key={m.id}>
              <strong>{m.title}</strong>
              {m.lessons.map((l) => (
                <button
                  key={l.id}
                  aria-current={lesson?.id === l.id ? "true" : undefined}
                  onClick={() => setSelected(l.id)}
                >
                  {l.position}. {l.title}
                </button>
              ))}
            </section>
          ))}
        </aside>
        <section className="sporg-card sporg-preview-content">
          <h2>{lesson?.title ?? "No lessons yet"}</h2>
          <p className="sporg-lesson-body">{lesson?.body}</p>
          {resources.map((r) => (
            <article key={r.id}>
              <h3>{r.title}</h3>
              <small>
                {r.kind} · {r.required ? "Required" : "Optional"} ·{" "}
                {r.preview ? "Preview enabled" : "Enrollment required"}
              </small>
              {r.kind === "ARTICLE" ? (
                <p className="sporg-lesson-body">{r.body}</p>
              ) : r.url ? (
                <a
                  href={r.url.startsWith("https://") ? r.url : undefined}
                  target="_blank"
                  rel="noreferrer"
                >
                  Open learning link ↗
                </a>
              ) : (
                <FilePreview resource={r} />
              )}
            </article>
          ))}
          <h3>Assessments</h3>
          <RemoteState {...assessments} retry={assessments.reload} />
          {assessments.data
            ?.filter(
              (a) =>
                a.ownerScope === "COURSE" ||
                a.ownerId === lesson?.id ||
                a.ownerId === lesson?.moduleId,
            )
            .map((a) => (
              <details key={a.id}>
                <summary>
                  {a.title} · {a.kind} · pass {a.pass_percent}%
                </summary>
                <AssessmentPreview id={a.id} />
              </details>
            ))}
          <h3>Assignments</h3>
          <RemoteState {...assignments} retry={assignments.reload} />
          {assignments.data
            ?.filter(
              (a) =>
                a.ownerScope === "COURSE" ||
                a.ownerId === lesson?.id ||
                a.ownerId === lesson?.moduleId,
            )
            .map((a) => (
              <article key={a.id}>
                <h4>{a.title}</h4>
                <p className="sporg-lesson-body">{a.instructions}</p>
                <small>
                  {a.required ? "Required" : "Optional"} · pass {a.passPercent}%
                  · {a.maxSubmissions} submissions
                </small>
                <AssignmentRubricPreview id={a.id} />
              </article>
            ))}
        </section>
      </div>
    </>
  );
}
function AssessmentPreview({ id }: { id: string }) {
  const remote = useRemote(
    useCallback(() => quizApi.assessmentQuestions(id), [id]),
  );
  return (
    <>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data?.map((q) => (
        <div className="sporg-row" key={q.question_version_id}>
          <p>
            {q.position}. {q.stem}
          </p>
          <small>
            {q.points} points · revision {q.question_version_id}
          </small>
          <QuestionPreview id={q.question_version_id} />
        </div>
      ))}
      <p>
        Author preview only. Open the assessment editor for answer keys; learner
        answers are never submitted here.
      </p>
    </>
  );
}

function QuestionPreview({ id }: { id: string }) {
  const remote = useRemote(
    useCallback(
      () =>
        import("@/shared/api/apiClient").then(({ apiClient }) =>
          apiClient.get<{
            stem: string;
            options: { id: string; body: string; correct: boolean }[];
          }>(`/quiz/question-versions/${id}/author-preview`),
        ),
      [id],
    ),
  );
  return (
    <>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data?.options.map((o) => (
        <p key={o.id}>
          {o.correct ? "✓" : "○"} {o.body}
          {o.correct && " (answer key — author only)"}
        </p>
      ))}
    </>
  );
}

function AssignmentRubricPreview({ id }: { id: string }) {
  const remote = useRemote(useCallback(() => assignmentApi.rubric(id), [id]));
  return (
    <>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data?.length === 0 && <p>Grading uses an overall score.</p>}
      {remote.data && remote.data.length > 0 && (
        <ul>
          {remote.data.map((c) => (
            <li key={c.id}>
              {c.title} · {c.weight}%
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
