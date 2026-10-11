import { CourseVersionComparison } from "../components/CourseVersionComparison";
import { CourseArtworkPanel } from "../components/CourseArtworkPanel";
import { CoursePreview } from "../components/CoursePreview";
import { CourseReadinessPanel } from "../components/CourseReadinessPanel";
import { CourseEnrollmentPanel } from "../components/CourseEnrollmentPanel";
import { CourseCommercialPanel } from "@/features/pricing/components/CourseCommercialPanel";
import { useState, useCallback, useEffect } from "react";
import { Link, useParams } from "react-router-dom";
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { useAction, useRemote } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import { workspaceCourseApi } from "../api/workspaceCourseApi";
import { courseApi } from "../api/courseApi";
import { useCourseStudio } from "../hooks/useCourseStudio";
import { CourseOutlineEditor } from "../components/CourseOutlineEditor";
import { CourseSettings } from "../components/CourseSettings";
import { CourseAssessments } from "@/features/quiz/components/CourseAssessments";
import { CourseAssignments } from "@/features/assignment/components/CourseAssignments";
export default function CourseStudioPage() {
  const { id = "" } = useParams();
  const { organization, confirmDiscard } = useOrganizationContext();
  const studio = useCourseStudio(id, organization?.id ?? "");
  const [tab, setTab] = useState("outline");
  const [settingsDirty, setSettingsDirty] = useState(false);
  const canLeave = () => {
    const ok = confirmDiscard(
      "Discard unsaved course form changes before switching?",
    );
    if (ok) setSettingsDirty(false);
    return ok;
  };
  const [target, setTarget] = useState<string | null>(null);
  const readiness = useRemote(
    useCallback(
      () =>
        studio.versionId
          ? workspaceCourseApi.readiness(studio.versionId)
          : Promise.resolve(null),
      [studio.versionId, studio.outline.data],
    ),
  );
  useEffect(() => {
    if (!target) return;
    const timer = setTimeout(() => {
      document
        .getElementById(`lesson-${target}`)
        ?.scrollIntoView({ behavior: "smooth", block: "center" });
      document
        .getElementById(`resource-${target}`)
        ?.scrollIntoView({ behavior: "smooth", block: "center" });
      setTarget(null);
    }, 80);
    return () => clearTimeout(timer);
  }, [target, tab]);
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
      <div data-course-studio />
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
                  if (canLeave()) setTab("publish");
                }}
              >
                Review & publish
              </Button>
            )}
          </div>
        </div>
      )}
      {value && (
        <section className="sporg-card sporg-course-wizard">
          <h2>Course creation workflow</h2>
          <p>
            Each step describes saved data. Optional activities do not become
            mandatory just because they exist.{" "}
            {settingsDirty
              ? "Unsaved settings — save before preview or publish."
              : "Preview shows server-saved content."}
          </p>
          <ol>
            {[
              {
                label: "Information",
                tab: "settings",
                done: Boolean(
                  studio.header.data?.course.title &&
                  studio.header.data?.course.summary,
                ),
                hint: "Explain the audience and outcome; set a course cover.",
              },
              {
                label: "Structure & content",
                tab: "outline",
                done:
                  value.modules.some((m) => m.lessons.length > 0) &&
                  !readiness.data?.issues.some((i) => i.tab === "outline"),
                hint: "Create modules and lessons, then attach actual resources.",
              },
              {
                label: "Assessments & assignments",
                tab: "assessments",
                done: !readiness.data?.issues.some(
                  (i) => i.tab === "assessments",
                ),
                hint: "Choose practice or official assessments; assignments are optional unless marked required.",
              },
              {
                label: "Completion",
                tab: "settings",
                done: Boolean(value.policy),
                hint: "Select required evidence independently of certification access.",
              },
              {
                label: "Access & pricing",
                tab: "pricing",
                done: Boolean(value.offer),
                hint: "Review base/effective prices and whether organizational grants are needed.",
              },
              {
                label: "Review & publish",
                tab: "publish",
                done: !draft,
                hint: "Preview as a learner, resolve checklist issues and publish an immutable version.",
              },
            ].map((step, i) => (
              <li key={step.label}>
                <button
                  onClick={() => {
                    if (canLeave()) setTab(step.tab);
                  }}
                >
                  <span>{i + 1}</span>
                  <strong>{step.label}</strong>
                  <small>
                    {readiness.loading
                      ? "Checking saved content"
                      : step.done
                        ? "Configured"
                        : "Needs attention"}
                  </small>
                </button>
                <p>{step.hint}</p>
              </li>
            ))}
          </ol>
        </section>
      )}
      {studio.header.data && (
        <div className="sporg-toolbar">
          <strong>
            {draft ? "Draft editor" : "Published / archived view"}
          </strong>
          <div className="sporg-actions">
            <Button
              variant="outline"
              onClick={() => {
                if (canLeave()) setTab("preview");
              }}
            >
              {draft ? "Preview saved draft" : "View this published version"}
            </Button>
            <Button
              variant="outline"
              disabled={
                !studio.header.data.versions.some(
                  (v) => v.status === "PUBLISHED",
                )
              }
              onClick={() => {
                if (canLeave()) {
                  studio.selectVersion(
                    studio.header.data!.versions.find(
                      (v) => v.status === "PUBLISHED",
                    )!.id,
                  );
                  setTab("preview");
                }
              }}
            >
              View latest publish
            </Button>
            <Button
              variant="outline"
              disabled={
                !studio.header.data.versions.some((v) => v.status === "DRAFT")
              }
              onClick={() => {
                if (canLeave()) {
                  studio.selectVersion(
                    studio.header.data!.versions.find(
                      (v) => v.status === "DRAFT",
                    )!.id,
                  );
                  setTab("outline");
                }
              }}
            >
              Edit draft
            </Button>
          </div>
        </div>
      )}
      <div
        className="sporg-local-tabs"
        role="tablist"
        aria-label="Course studio"
      >
        {[
          "outline",
          "assessments",
          "assignments",
          "settings",
          "preview",
          "pricing",
          "learners",
          "compare",
          "publish",
        ].map((t) => (
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
          <div className="sporg-studio-layout">
            <aside className="sporg-outline-nav">
              <h3>Course outline</h3>
              {value.modules.length === 0 ? (
                <p>Add your first module in Structure.</p>
              ) : (
                value.modules.map((m) => (
                  <section key={m.id}>
                    <strong>
                      {m.position}. {m.title}
                    </strong>
                    {m.lessons.map((l) => (
                      <button
                        className="sporg-outline-link"
                        key={l.id}
                        onClick={() => {
                          if (canLeave()) {
                            setTab("outline");
                            setTarget(l.id);
                          }
                        }}
                      >
                        {l.position}. {l.title}
                      </button>
                    ))}
                  </section>
                ))
              )}
            </aside>
            <div>
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
              {tab === "compare" && (
                <CourseVersionComparison
                  versions={studio.header.data!.versions}
                  current={studio.versionId}
                />
              )}
              {tab === "preview" && (
                <CoursePreview
                  course={studio.header.data!.course}
                  outline={value}
                />
              )}
              {tab === "pricing" && (
                <CourseCommercialPanel
                  versionId={studio.versionId}
                  onDirtyChange={setSettingsDirty}
                />
              )}
              {tab === "learners" && (
                <CourseEnrollmentPanel versionId={studio.versionId} />
              )}
              {tab === "publish" && (
                <CourseReadinessPanel
                  versionId={studio.versionId}
                  navigate={(next, id) => {
                    if (canLeave()) {
                      setTab(next);
                      setTarget(id ?? null);
                    }
                  }}
                  published={studio.reload}
                />
              )}
              {tab === "settings" && (
                <>
                  <CourseArtworkPanel courseId={id} />
                  <CourseSettings
                    course={studio.header.data!.course}
                    outline={value}
                    refresh={studio.reload}
                    onDirtyChange={setSettingsDirty}
                  />
                </>
              )}
            </div>
          </div>
        </div>
      )}
    </OrganizerSurface>
  );
}
