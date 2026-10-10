import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import { courseApi } from "../api/courseApi";
import { useCourseStudio } from "../hooks/useCourseStudio";
import { CourseOutlineEditor } from "../components/CourseOutlineEditor";
import { CourseSettings } from "../components/CourseSettings";
import { CourseAssessments } from "@/features/quiz/components/CourseAssessments";
import { CourseAssignments } from "@/features/assignment/components/CourseAssignments";
export default function CourseStudioPage() {
  const { id = "" } = useParams();
  const { organization } = useOrganizationContext();
  const studio = useCourseStudio(id, organization?.id ?? "");
  const [tab, setTab] = useState("outline");
  const [settingsDirty, setSettingsDirty] = useState(false);
  const canLeave = () =>
    !settingsDirty || window.confirm("Discard unsaved course settings?");
  const action = useAction(studio.reload);
  const value = studio.outline.data;
  const draft = value?.version.status === "DRAFT";
  const owners = value
    ? [
        {
          scope: "COURSE" as const,
          id: studio.versionId,
          label: "Entire course",
        },
        ...value.modules.flatMap((m) => [
          { scope: "MODULE" as const, id: m.id, label: m.title },
          ...m.lessons.map((l) => ({
            scope: "LESSON" as const,
            id: l.id,
            label: `${m.title} / ${l.title}`,
          })),
        ]),
      ]
    : [];
  return (
    <OrganizerSurface
      title={studio.header.data?.course.title ?? "Course studio"}
      description="Build the outline, assessments and completion rules for this course."
      authority="MANAGE_CONTENT"
    >
      <Link className="sporg-back" to="/organizer/courses">
        ← All courses
      </Link>
      <RemoteState
        loading={studio.header.loading}
        error={studio.header.error}
        retry={studio.header.reload}
      />
      <ActionNotice {...action} />
      {studio.header.data && (
        <div className="sporg-toolbar">
          <label>
            Version
            <select
              value={studio.versionId}
              disabled={action.busy}
              onChange={(e) => {
                if (canLeave()) studio.selectVersion(e.target.value);
              }}
            >
              {studio.header.data.versions.map((v) => (
                <option key={v.id} value={v.id}>
                  v{v.version_no} · {v.status.toLowerCase()}
                </option>
              ))}
            </select>
          </label>
          <div className="sporg-actions">
            {!studio.header.data.versions.some((v) => v.status === "DRAFT") && (
              <Button
                disabled={action.busy}
                variant="outline"
                onClick={() =>
                  void action.run(
                    () => courseApi.clone(id),
                    "New draft created.",
                  )
                }
              >
                Create next draft
              </Button>
            )}
            {draft && (
              <Button
                disabled={action.busy || studio.outline.loading}
                onClick={() => {
                  if (
                    window.confirm(
                      "Publish this version? Its content becomes immutable. Check resources and assessments first.",
                    )
                  )
                    void action.run(
                      () => courseApi.publish(studio.versionId),
                      "Course published.",
                    );
                }}
              >
                Publish version
              </Button>
            )}
          </div>
        </div>
      )}
      <div
        className="sporg-local-tabs"
        role="tablist"
        aria-label="Course studio"
      >
        {["outline", "assessments", "assignments", "settings"].map((t) => (
          <button
            key={t}
            role="tab"
            id={`studio-tab-${t}`}
            aria-selected={tab === t}
            aria-controls="studio-panel"
            onClick={() => {
              if (t === tab || canLeave()) setTab(t);
            }}
          >
            {t[0].toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>
      <RemoteState
        loading={Boolean(studio.versionId) && studio.outline.loading}
        error={studio.outline.error}
        retry={studio.outline.reload}
      />
      {value && (
        <div
          id="studio-panel"
          role="tabpanel"
          aria-labelledby={`studio-tab-${tab}`}
          key={studio.versionId}
        >
          {!draft && (
            <p className="sporg-success">
              This version is {value.version.status.toLowerCase()}. Content is
              read-only.
            </p>
          )}
          {tab === "outline" && (
            <CourseOutlineEditor
              outline={value}
              refresh={studio.outline.reload}
            />
          )}
          {tab === "assessments" && (
            <CourseAssessments
              versionId={studio.versionId}
              owners={owners}
              editable={draft}
            />
          )}
          {tab === "assignments" && (
            <CourseAssignments
              versionId={studio.versionId}
              owners={owners}
              editable={draft}
            />
          )}
          {tab === "settings" && (
            <CourseSettings
              course={studio.header.data!.course}
              outline={value}
              refresh={studio.reload}
              onDirtyChange={setSettingsDirty}
            />
          )}
        </div>
      )}
    </OrganizerSurface>
  );
}
