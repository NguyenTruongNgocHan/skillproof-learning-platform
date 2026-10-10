import { certificationApi } from "@/features/certification/api/certificationApi";
import type {
  Certificate,
  Eligibility,
  Enrollment,
  Page,
  Program,
} from "@/features/certification/types/certification.types";
import { organizerError } from "@/features/organization/utils/errorMessage";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
import { useToast } from "@/shared/ui/Toast";
import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
export function EligibilityPanel({
  program,
  onIssued,
}: {
  program: Program;
  onIssued: () => void;
}) {
  const [query, setQuery] = useState("");
  const [page, setPage] = useState<Page<Enrollment> | null>(null);
  const [index, setIndex] = useState(0);
  const [enrollment, setEnrollment] = useState<Enrollment | null>(null);
  const [eligibility, setEligibility] = useState<Eligibility | null>(null);
  const [certificate, setCertificate] = useState<Certificate | null>(null);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const generation = useRef(0);
  const { toast } = useToast();
  useEffect(
    () => () => {
      generation.current++;
    },
    [],
  );
  function choose(row: Enrollment) {
    generation.current++;
    setEnrollment(row);
    setEligibility(null);
    setCertificate(null);
    setError("");
  }
  async function search(next = 0) {
    const req = ++generation.current;
    setBusy(true);
    setError("");
    setEnrollment(null);
    setEligibility(null);
    setCertificate(null);
    try {
      const rows = await certificationApi.enrollments(program.id, query, next);
      if (req === generation.current) {
        setPage(rows);
        setIndex(next);
      }
    } catch (e) {
      if (req === generation.current)
        setError(organizerError(e, "Search failed"));
    } finally {
      if (req === generation.current) setBusy(false);
    }
  }
  async function evaluate() {
    if (!enrollment) return;
    const req = ++generation.current;
    setBusy(true);
    setError("");
    setEligibility(null);
    setCertificate(null);
    try {
      const result = await certificationApi.evaluate(program.id, enrollment.id);
      if (req === generation.current) setEligibility(result);
    } catch (e) {
      if (req === generation.current)
        setError(organizerError(e, "Evaluation failed"));
    } finally {
      if (req === generation.current) setBusy(false);
    }
  }
  async function issue() {
    if (
      !eligibility ||
      eligibility.status !== "ELIGIBLE" ||
      !enrollment ||
      eligibility.enrollmentId !== enrollment.id
    )
      return;
    if (!window.confirm(`Issue ${program.name} to ${enrollment.learnerEmail}?`))
      return;
    const req = ++generation.current;
    setBusy(true);
    setError("");
    try {
      const result = await certificationApi.issue(eligibility.id);
      if (req === generation.current) {
        setCertificate(result);
        onIssued();
        toast(
          "success",
          result.status === "REVOKED"
            ? "An existing revoked certificate was found. It cannot be issued again."
            : "Certificate issuance complete.",
        );
      }
    } catch (e) {
      if (req === generation.current)
        setError(organizerError(e, "Issuance failed"));
    } finally {
      if (req === generation.current) setBusy(false);
    }
  }
  let evidence: {
    totalResources?: number;
    completedResources?: number;
    requiredAssessments?: number;
    passedAssessments?: number;
    requiredAssignments?: number;
    passedAssignments?: number;
    requiredLessons?: number;
    completedLessons?: number;
    completed?: boolean;
  } | null = null;
  try {
    if (eligibility) evidence = JSON.parse(eligibility.evidenceSnapshotJson);
  } catch {
    /* Invalid evidence is not interpreted as passing. */
  }
  return (
    <section className="sporg-card">
      <p className="sporg-eyebrow">Eligibility and issuance</p>
      <h2>{program.name}</h2>
      {error && (
        <p className="sporg-alert" role="alert">
          {error}
        </p>
      )}
      <form
        className="sporg-form"
        onSubmit={(e) => {
          e.preventDefault();
          void search();
        }}
      >
        <Input
          label="Find learner by email or ID"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <Button type="submit" disabled={busy}>
          Search enrollments
        </Button>
      </form>
      {page && (
        <>
          <p>{page.totalElements} matching enrollments</p>
          {page.content.map((row) => (
            <div className="sporg-row" key={row.id}>
              <div>
                <strong>{row.learnerEmail}</strong>
                <small>
                  {row.pathTitle} · version {row.versionNo} · {row.status}
                </small>
              </div>
              <Button
                variant={enrollment?.id === row.id ? "primary" : "outline"}
                disabled={busy}
                onClick={() => choose(row)}
              >
                {enrollment?.id === row.id ? "Selected" : "Select"}
              </Button>
            </div>
          ))}
          <div className="sporg-actions">
            <Button
              variant="outline"
              disabled={busy || index === 0}
              onClick={() => void search(index - 1)}
            >
              Previous
            </Button>
            <span>
              Page {index + 1} / {Math.max(1, page.totalPages)}
            </span>
            <Button
              variant="outline"
              disabled={busy || page.last}
              onClick={() => void search(index + 1)}
            >
              Next
            </Button>
          </div>
        </>
      )}
      {enrollment && (
        <div className="sporg-actions">
          <strong>{enrollment.learnerEmail}</strong>
          <Button disabled={busy} onClick={() => void evaluate()}>
            {busy ? "Working…" : "Evaluate eligibility"}
          </Button>
        </div>
      )}
      {eligibility && (
        <>
          <h3>
            {eligibility.status === "ELIGIBLE"
              ? "All required evidence is complete"
              : "Requirements are still in progress"}
          </h3>
          {evidence ? (
            <dl>
              <dt>Resources</dt>
              <dd>
                {evidence.completedResources} / {evidence.totalResources}
              </dd>
              <dt>Lessons</dt>
              <dd>
                {evidence.completedLessons ?? "—"} /{" "}
                {evidence.requiredLessons ?? "—"}
              </dd>
              <dt>Required assignments</dt>
              <dd>
                {evidence.passedAssignments ?? "—"} /{" "}
                {evidence.requiredAssignments ?? "—"}
              </dd>
              <dt>Official assessments</dt>
              <dd>
                {evidence.passedAssessments} / {evidence.requiredAssessments}
              </dd>
            </dl>
          ) : (
            <p>
              Evidence details are unavailable. Contact support before issuing.
            </p>
          )}
          <p>Evaluated {new Date(eligibility.evaluatedAt).toLocaleString()}</p>
          {eligibility.status === "ELIGIBLE" && !certificate && (
            <Button disabled={busy} onClick={() => void issue()}>
              Issue certificate
            </Button>
          )}
        </>
      )}
      {certificate && (
        <p>
          <Link to={`/organizer/certificates/${certificate.id}`}>
            View certificate {certificate.serialNumber}
          </Link>
        </p>
      )}
    </section>
  );
}
