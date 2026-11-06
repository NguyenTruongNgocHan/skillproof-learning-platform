import { useCallback } from "react"
import { apiClient } from "@/shared/api/apiClient"
import { useRemote } from "@/shared/hooks/useRemote"
import { RemoteState } from "@/shared/ui/RemoteState"
interface PreviewResource {
  id: string
  lessonId: string
  position: number
  kind: string
  title: string
  body: string | null
  url: string | null
}
export function PublicCoursePreview({ courseId }: { courseId: string }) {
  const remote = useRemote(
    useCallback(
      () => apiClient.get<PreviewResource[]>(`/courses/${encodeURIComponent(courseId)}/preview`),
      [courseId],
    ),
  )
  return (
    <section className="sporg-card">
      <h2>Public preview</h2>
      <p>Only resources selected for public preview are shown here.</p>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data?.length === 0 && <p>No preview resources selected.</p>}
      {remote.data?.map((resource) => (
        <article key={resource.id}>
          <h3>{resource.title}</h3>
          <p className="sporg-lesson-body">{resource.body}</p>
          {resource.url && /^https?:\/\//i.test(resource.url) && (
            <a href={resource.url} target="_blank" rel="noreferrer">
              Open preview resource
            </a>
          )}
          {["FILE", "AUDIO", "VIDEO", "IMAGE"].includes(resource.kind) && (
            <PreviewFiles resourceId={resource.id} />
          )}
        </article>
      ))}
    </section>
  )
}
function PreviewFiles({ resourceId }: { resourceId: string }) {
  const remote = useRemote(
    useCallback(
      () =>
        apiClient.get<{ id: string; name: string }[]>(
          `/public/course-previews/${encodeURIComponent(resourceId)}/files`,
        ),
      [resourceId],
    ),
  )
  const base = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api/v1"
  return (
    <>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data?.map((file) => (
        <p key={file.id}>
          <a
            href={`${base}/public/course-previews/${encodeURIComponent(resourceId)}/files/${encodeURIComponent(file.id)}`}
          >
            Download {file.name}
          </a>
        </p>
      ))}
    </>
  )
}
