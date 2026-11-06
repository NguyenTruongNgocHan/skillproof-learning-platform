import { useCallback } from "react"
import type { CourseOutline } from "@/features/course/types/course.types"
import { quizApi } from "@/features/quiz/api/quizApi"
import { questionBankApi } from "@/features/quiz/api/questionBankApi"
import { assignmentApi } from "@/features/assignment/api/assignmentApi"
import { useRemote } from "@/shared/hooks/useRemote"
import { RemoteState } from "@/shared/ui/RemoteState"
import MediaPanel from "@/features/media/components/MediaPanel"
export default function CourseReviewOutline({ outline }: { outline: CourseOutline }) {
  return (
    <div className="platform-stack">
      <h3>
        Version {outline.version.version_no} · {outline.version.status}
      </h3>
      <p>
        All lessons and required resources must be completed.{" "}
        {outline.policy.require_official_assessments
          ? "Required official assessments apply."
          : "No global official assessment requirement."}
      </p>
      {outline.modules.map((module) => (
        <section key={module.id}>
          <h3>
            {module.position}. {module.title}
          </h3>
          {module.lessons.map((lesson) => (
            <article key={lesson.id} className="sporg-card">
              <h4>
                {lesson.position}. {lesson.title}
              </h4>
              <p className="sporg-lesson-body">{lesson.body}</p>
              {module.resources
                .filter((r) => r.lesson_id === lesson.id)
                .map((resource) => (
                  <div key={resource.id}>
                    <strong>{resource.title}</strong>
                    <p>
                      {resource.kind} · {resource.required ? "Required" : "Optional"} ·{" "}
                      {resource.preview ? "Public preview" : "Protected content"}
                    </p>
                    <p className="sporg-lesson-body">{resource.body}</p>
                    {resource.url && /^https?:\/\//i.test(resource.url) && (
                      <a href={resource.url} target="_blank" rel="noreferrer">
                        Open resource
                      </a>
                    )}
                    {["FILE", "IMAGE", "AUDIO", "VIDEO"].includes(resource.kind) && (
                      <MediaPanel scope="RESOURCE" target={resource.id} />
                    )}
                  </div>
                ))}
            </article>
          ))}
        </section>
      ))}
      <Activities versionId={outline.version.id} />
    </div>
  )
}
function Activities({ versionId }: { versionId: string }) {
  const remote = useRemote(
    useCallback(
      async () => ({
        assessments: await quizApi.assessments(versionId),
        assignments: await assignmentApi.list(versionId),
      }),
      [versionId],
    ),
  )
  return (
    <section>
      <h3>Assessments & assignments</h3>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data?.assessments.map((a) => (
        <details key={a.id}>
          <summary>
            {a.title} · {a.kind} · {a.status} · {a.required ? "Required" : "Optional"}
          </summary>
          <p>
            Pass {a.pass_percent}% · {a.duration_seconds} seconds · {a.max_attempts} attempts
          </p>
          <AssessmentQuestions id={a.id} />
        </details>
      ))}
      {remote.data?.assignments.map((a) => (
        <article key={a.id}>
          <h4>{a.title}</h4>
          <p>{a.instructions}</p>
          <p>
            {a.ownerScope} · {a.required ? "Required" : "Optional"} · Pass {a.passPercent}% ·{" "}
            {a.maxSubmissions} submissions
          </p>
          <p>{a.dueAt ? new Date(a.dueAt).toLocaleString() : "No deadline"}</p>
        </article>
      ))}
    </section>
  )
}
function AssessmentQuestions({ id }: { id: string }) {
  const remote = useRemote(useCallback(() => quizApi.assessmentQuestions(id), [id]))
  return (
    <>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data?.map((question) => (
        <details key={question.question_version_id}>
          <summary>
            {question.position}. {question.stem} · {question.points} points
          </summary>
          <AuthorPreview id={question.question_version_id} />
        </details>
      ))}
    </>
  )
}
function AuthorPreview({ id }: { id: string }) {
  const remote = useRemote(useCallback(() => questionBankApi.preview(id), [id]))
  return (
    <>
      <RemoteState {...remote} retry={remote.reload} />
      <ul>
        {remote.data?.options.map((o) => (
          <li key={o.id}>
            {o.body}
            {o.correct && " · Correct answer"}
          </li>
        ))}
      </ul>
    </>
  )
}
