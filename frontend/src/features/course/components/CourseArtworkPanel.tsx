import { useCallback, useEffect, useState } from "react";
import { apiClient } from "@/shared/api/apiClient";
import MediaPanel from "@/features/media/components/MediaPanel";
import { mediaApi } from "@/features/media/api/mediaApi";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
export function CourseArtworkPanel({ courseId }: { courseId: string }) {
  const [cover, setCover] = useState("");
  const remote = useRemote(
    useCallback(
      () =>
        apiClient.get<{ coverMediaId?: string }>(
          `/course-workspace/courses/${courseId}/artwork`,
        ),
      [courseId],
    ),
  );
  const files = useRemote(
    useCallback(() => mediaApi.list("COURSE_COVER", courseId), [courseId]),
  );
  const action = useAction(remote.reload);
  useEffect(() => {
    setCover(remote.data?.coverMediaId ?? "");
  }, [remote.data]);
  return (
    <section className="sporg-card">
      <h2>Course cover</h2>
      <p>
        Use a real cover image for the public catalog. This marketing image is
        separate from versioned lesson content.
      </p>
      <MediaPanel
        scope="COURSE_COVER"
        target={courseId}
        writable
        onChanged={files.reload}
      />
      <RemoteState {...remote} retry={remote.reload} />
      <ActionNotice {...action} />
      <label>
        Catalog image
        <select value={cover} onChange={(e) => setCover(e.target.value)}>
          <option value="">No cover</option>
          {files.data?.map((f) => (
            <option key={f.id} value={f.id}>
              {f.name}
            </option>
          ))}
        </select>
      </label>
      <Button
        disabled={action.busy}
        onClick={() =>
          void action.run(
            () =>
              apiClient.put(`/course-workspace/courses/${courseId}/artwork`, {
                coverMediaId: cover || null,
              }),
            "Cover updated.",
          )
        }
      >
        Apply cover
      </Button>
    </section>
  );
}
