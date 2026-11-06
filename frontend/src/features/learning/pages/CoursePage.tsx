import { useCallback } from "react"
import { Link, useParams } from "react-router-dom"
import LearnerShell from "@/shared/components/layout/LearnerShell"
import { useRemote, useAction } from "@/shared/hooks/useRemote"
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState"
import Button from "@/shared/ui/Button"
import { learningApi } from "../api/learningApi"
import MediaPanel from "@/features/media/components/MediaPanel"
import { quizApi } from "@/features/quiz/api/quizApi"
import { useNavigate } from "react-router-dom"
/** Existing learner content is adapted to the nested contract; learner assignment authoring is outside this release. */
export default function CoursePage() {
  const { id = "" } = useParams()
  const navigate = useNavigate()
  const remote = useRemote(useCallback(() => learningApi.course(id), [id]))
  const assessments = useRemote(useCallback(() => quizApi.available(id), [id]))
  const action = useAction(remote.reload)
  return (
    <LearnerShell>
      <section className="sporg">
        <Link to="/app/learning">← My learning</Link>
        <RemoteState {...remote} retry={remote.reload} />
        <ActionNotice {...action} />
        {remote.data && (
          <>
            <header className="sporg-heading">
              <h1>{remote.data.enrollment.title}</h1>
              <p>{remote.data.enrollment.summary}</p>
              <p>
                Version {remote.data.enrollment.version_no} · {remote.data.enrollment.status}
              </p>
            </header>
            <p>
              Required resources: {remote.data.progress.completedResources}/
              {remote.data.progress.totalResources}. Completion is evaluated by the server from
              lessons, required resources and required activities.
            </p>
            {remote.data.modules.map((module) => (
              <section key={module.id} className="sporg-card">
                <h2>
                  {module.position}. {module.title}
                </h2>
                {module.lessons.map((lesson) => (
                  <section key={lesson.id} className="platform-stack">
                    <h3>
                      {lesson.position}. {lesson.title}
                    </h3>
                    <p className="sporg-lesson-body">{lesson.body}</p>
                    {lesson.resources.map((resource) => (
                      <article key={resource.id} className="sporg-card">
                        <h4>{resource.title}</h4>
                        <small>
                          {resource.required ? "Required" : "Optional"} ·{" "}
                          {resource.completed ? "Completed" : "Not completed"}
                        </small>
                        <p className="sporg-lesson-body">{resource.body}</p>
                        {resource.url && /^https?:\/\//i.test(resource.url) && (
                          <a href={resource.url} target="_blank" rel="noreferrer">
                            Open resource
                          </a>
                        )}
                        {["FILE", "VIDEO", "AUDIO", "IMAGE"].includes(resource.kind) && (
                          <MediaPanel scope="RESOURCE" target={resource.id} />
                        )}
                        <Button
                          variant="outline"
                          disabled={action.busy || resource.completed}
                          onClick={() =>
                            void action.run(
                              () => learningApi.complete(id, resource.id),
                              "Resource completion recorded.",
                            )
                          }
                        >
                          Complete resource
                        </Button>
                      </article>
                    ))}
                    <Button
                      disabled={
                        action.busy ||
                        lesson.completed ||
                        lesson.resources.some((r) => r.required && !r.completed)
                      }
                      onClick={() =>
                        void action.run(
                          () => learningApi.completeLesson(id, lesson.id),
                          "Lesson completion recorded.",
                        )
                      }
                    >
                      {lesson.completed ? "Lesson completed" : "Complete lesson"}
                    </Button>
                  </section>
                ))}
              </section>
            ))}
          </>
        )}
        <section className="sporg-card">
          <h2>Assessments</h2>
          <RemoteState {...assessments} retry={assessments.reload} />
          {assessments.data?.length === 0 && <p>No assessments available.</p>}
          {assessments.data?.map((a) => (
            <div className="sporg-row" key={a.id}>
              <div>
                <strong>{a.title}</strong>
                <small>
                  {a.kind} · {a.required ? "Required" : "Optional"}
                </small>
              </div>
              <Button
                disabled={action.busy}
                onClick={() =>
                  void action.run(async () => {
                    const result = await quizApi.start(a.id, id)
                    navigate(`/app/attempts/${result.attempt.id}`)
                  }, "Attempt ready.")
                }
              >
                Start / resume
              </Button>
            </div>
          ))}
        </section>
      </section>
    </LearnerShell>
  )
}
