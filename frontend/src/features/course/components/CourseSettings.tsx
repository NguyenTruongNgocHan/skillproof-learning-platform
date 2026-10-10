import { useState, useEffect } from "react";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { useAction } from "@/shared/hooks/useRemote";
import { ActionNotice } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
import type { CourseRecord, CourseOutline } from "../types/course.types";
import { courseApi } from "../api/courseApi";
export function CourseSettings({
  course,
  outline,
  refresh,
  onDirtyChange,
}: {
  course: CourseRecord;
  outline: CourseOutline;
  refresh: () => void;
  onDirtyChange?: (dirty: boolean) => void;
}) {
  const action = useAction(refresh);
  const editable = outline.version.status === "DRAFT";
  const [title, setTitle] = useState(course.title),
    [summary, setSummary] = useState(course.summary);
  const [resources, setResources] = useState(
      outline.policy.require_all_resources,
    ),
    [assessments, setAssessments] = useState(
      outline.policy.require_official_assessments,
    );
  const [mode, setMode] = useState(""),
    [price, setPrice] = useState(""),
    [certPrice, setCertPrice] = useState("");
  const { setDirty } = useOrganizationContext();
  const dirty =
    title !== course.title ||
    summary !== course.summary ||
    resources !== outline.policy.require_all_resources ||
    assessments !== outline.policy.require_official_assessments ||
    Boolean(mode || price || certPrice);
  useEffect(() => {
    onDirtyChange?.(dirty);
    return () => onDirtyChange?.(false);
  }, [dirty, onDirtyChange]);
  useEffect(() => {
    setDirty(
      "course-settings",
      title !== course.title ||
        summary !== course.summary ||
        resources !== outline.policy.require_all_resources ||
        assessments !== outline.policy.require_official_assessments ||
        Boolean(mode || price || certPrice),
    );
    return () => setDirty("course-settings", false);
  }, [
    title,
    summary,
    resources,
    assessments,
    mode,
    price,
    certPrice,
    course,
    outline,
    setDirty,
  ]);
  return (
    <>
      <ActionNotice {...action} />
      <div className="sporg-grid">
        <section className="sporg-card">
          <h2>Course details</h2>
          <form
            className="sporg-form"
            onSubmit={(e) => {
              e.preventDefault();
              void action.run(() =>
                courseApi.edit(course.id, { title, summary }),
              );
            }}
          >
            <Input
              label="Title"
              required
              maxLength={180}
              value={title}
              disabled={!editable || action.busy}
              onChange={(e) => setTitle(e.target.value)}
            />
            <label>
              Summary
              <textarea
                required
                maxLength={1000}
                value={summary}
                disabled={!editable || action.busy}
                onChange={(e) => setSummary(e.target.value)}
              />
            </label>
            {editable && (
              <Button type="submit" disabled={action.busy}>
                Save details
              </Button>
            )}
          </form>
        </section>
        <section className="sporg-card">
          <h2>Completion rules</h2>
          <form
            className="sporg-form"
            onSubmit={(e) => {
              e.preventDefault();
              void action.run(() =>
                courseApi.policy(outline.version.id, {
                  requireAllResources: resources,
                  requireOfficialAssessments: assessments,
                }),
              );
            }}
          >
            <label className="sporg-checkbox">
              <input
                type="checkbox"
                checked={resources}
                disabled={!editable || action.busy}
                onChange={(e) => setResources(e.target.checked)}
              />{" "}
              Require resource completion
            </label>
            <label className="sporg-checkbox">
              <input
                type="checkbox"
                checked={assessments}
                disabled={!editable || action.busy}
                onChange={(e) => setAssessments(e.target.checked)}
              />{" "}
              Require official assessments
            </label>
            <p>
              Required assignments and activities also contribute to completion.
              Course completion does not automatically issue a certificate.
            </p>
            {editable && (
              <Button type="submit" disabled={action.busy}>
                Save completion policy
              </Button>
            )}
          </form>
        </section>
        <section className="sporg-card sporg-full">
          <h2>Access and pricing</h2>
          <p>
            Enter all fields explicitly to replace the offer. Existing offer
            values are not available here; blank fields do not mean free access.
          </p>
          {editable ? (
            <form
              className="sporg-form"
              onSubmit={(e) => {
                e.preventDefault();
                void action.run(async () => {
                  await courseApi.offer(outline.version.id, {
                    accessMode: mode,
                    priceVnd: Number(price),
                    certificationPriceVnd: Number(certPrice),
                  });
                  setMode("");
                  setPrice("");
                  setCertPrice("");
                }, "Offer saved.");
              }}
            >
              <div className="sporg-fields">
                <label>
                  Access mode
                  <select
                    required
                    value={mode}
                    disabled={action.busy}
                    onChange={(e) => {
                      setMode(e.target.value);
                      setPrice("");
                      setCertPrice("");
                    }}
                  >
                    <option value="">Choose access mode</option>
                    <option value="PUBLIC">Public: free or paid</option>
                    <option value="RESTRICTED">
                      Restricted: organization grants
                    </option>
                  </select>
                </label>
                <Input
                  label="Learning price (VND)"
                  type="number"
                  required
                  min={0}
                  max={mode === "RESTRICTED" ? 0 : 1000000000}
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                />
                <Input
                  label="Certification price (VND)"
                  type="number"
                  required
                  min={0}
                  max={mode === "RESTRICTED" ? 0 : 1000000000}
                  value={certPrice}
                  onChange={(e) => setCertPrice(e.target.value)}
                />
              </div>
              <p>
                Public prices: 0 or 5,000–1,000,000,000 VND. Restricted offers
                require both prices to be 0.
              </p>
              <Button type="submit" disabled={action.busy}>
                Replace offer
              </Button>
            </form>
          ) : (
            <p>Pricing can only be configured in a draft.</p>
          )}
        </section>
      </div>
    </>
  );
}
