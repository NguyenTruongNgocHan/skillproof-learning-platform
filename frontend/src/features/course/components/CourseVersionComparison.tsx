import { useState, useCallback } from "react";
import { courseApi } from "../api/courseApi";
import { quizApi } from "@/features/quiz/api/quizApi";
import { assignmentApi } from "@/features/assignment/api/assignmentApi";
import { mediaApi } from "@/features/media/api/mediaApi";
import type { CourseVersion } from "../types/course.types";
import { useRemote } from "@/shared/hooks/useRemote";
import { RemoteState } from "@/shared/ui/RemoteState";
async function snapshot(id: string) {
  const [outline, assessments, assignments] = await Promise.all([
    courseApi.outline(id),
    quizApi.assessments(id),
    assignmentApi.list(id),
  ]);
  const owners = new Map<string, string>([[outline.version.id, "COURSE"]]);
  outline.modules.forEach((m) => {
    owners.set(m.id, `MODULE:${m.position}`);
    m.lessons.forEach((l) =>
      owners.set(l.id, `MODULE:${m.position}/LESSON:${l.position}`),
    );
  });
  const modules = await Promise.all(
    outline.modules.map(async (m) => ({
      title: m.title,
      lessons: m.lessons.map((l) => ({
        position: l.position,
        title: l.title,
        body: l.body,
      })),
      resources: await Promise.all(
        m.resources.map(async (r) => ({
          position: r.position,
          lesson: r.lesson_id ? owners.get(r.lesson_id) : "INVALID_OWNER",
          kind: r.kind,
          title: r.title,
          body: r.body,
          url: r.url,
          required: r.required,
          preview: r.preview,
          files: (await mediaApi.list("RESOURCE", r.id)).map((f) => ({
            name: f.name,
            size: f.size,
            mimeType: f.mimeType,
            sha256: f.sha256 ?? null,
          })),
        })),
      ),
    })),
  );
  const quiz = await Promise.all(
    assessments.map(async (a) => ({
      title: a.title,
      kind: a.kind,
      required: a.required,
      ownerScope: a.ownerScope,
      owner: owners.get(a.ownerId ?? outline.version.id),
      duration: a.duration_seconds,
      pass: a.pass_percent,
      attempts: a.max_attempts,
      questions: (await quizApi.assessmentQuestions(a.id)).map((q) => ({
        version: q.question_version_id,
        points: q.points,
        position: q.position,
      })),
    })),
  );
  return {
    metadata: {
      title: outline.version.title,
      summary: outline.version.summary,
    },
    modules,
    offer: outline.offer,
    completion: outline.policy,
    assessments: quiz,
    assignments: await Promise.all(
      assignments.map(async (a) => ({
        title: a.title,
        instructions: a.instructions,
        required: a.required,
        scope: a.ownerScope,
        owner: owners.get(a.ownerId),
        pass: a.passPercent,
        max: a.maxSubmissions,
        due: a.dueAt,
        rubric: (await assignmentApi.rubric(a.id)).map((c) => ({
          title: c.title,
          weight: c.weight,
          position: c.position,
        })),
      })),
    ),
  };
}
export function CourseVersionComparison({
  versions,
  current,
}: {
  versions: CourseVersion[];
  current: string;
}) {
  const [baseline, setBaseline] = useState("");
  const remote = useRemote(
    useCallback(
      async () =>
        baseline
          ? { before: await snapshot(baseline), after: await snapshot(current) }
          : null,
      [baseline, current],
    ),
  );
  return (
    <section className="sporg-card">
      <h2>Compare course versions</h2>
      <p>
        Compare saved content, question revisions, assignment rules, completion
        policy and draft offers. File comparison uses SHA-256 when present, so
        files with matching names and sizes but different bytes are
        distinguished. Legacy files without a digest show null. Published
        commercial price history is separate.
      </p>
      <label>
        Compare current version against
        <select value={baseline} onChange={(e) => setBaseline(e.target.value)}>
          <option value="">Choose another version</option>
          {versions
            .filter((v) => v.id !== current)
            .map((v) => (
              <option key={v.id} value={v.id}>
                v{v.version_no} · {v.status}
              </option>
            ))}
        </select>
      </label>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data &&
        Object.keys(remote.data.before).map((key) => {
          const name = key as keyof typeof remote.data.before;
          const before = remote.data!.before[name],
            after = remote.data!.after[name];
          return (
            <details key={key}>
              <summary>
                {JSON.stringify(before) === JSON.stringify(after)
                  ? "Unchanged"
                  : "Changed"}{" "}
                · {key}
              </summary>
              <div className="sporg-grid">
                <section>
                  <h3>Baseline</h3>
                  <pre className="sporg-diff-json">
                    {JSON.stringify(before, null, 2)}
                  </pre>
                </section>
                <section>
                  <h3>Current</h3>
                  <pre className="sporg-diff-json">
                    {JSON.stringify(after, null, 2)}
                  </pre>
                </section>
              </div>
            </details>
          );
        })}
    </section>
  );
}
