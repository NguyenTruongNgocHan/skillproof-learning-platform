import { useCallback, useState } from "react";
import { courseApi } from "../api/courseApi";
import { useRemote } from "@/shared/hooks/useRemote";
export function useCourseStudio(courseId: string, organizationId: string) {
  const [selected, setSelected] = useState("");
  const header = useRemote(
    useCallback(async () => {
      const [courses, versions] = await Promise.all([
        courseApi.list(organizationId),
        courseApi.versions(courseId),
      ]);
      const course = courses.find((c) => c.id === courseId);
      if (!course)
        throw new Error(
          "This course does not belong to the selected organization.",
        );
      return { course, versions };
    }, [courseId, organizationId]),
  );
  const versionId =
    header.data?.versions.find((v) => v.id === selected)?.id ??
    header.data?.versions[0]?.id ??
    "";
  const outline = useRemote(
    useCallback(
      () => (versionId ? courseApi.outline(versionId) : Promise.resolve(null)),
      [versionId],
    ),
  );
  function reload() {
    header.reload();
    outline.reload();
  }
  return { header, outline, versionId, selectVersion: setSelected, reload };
}
