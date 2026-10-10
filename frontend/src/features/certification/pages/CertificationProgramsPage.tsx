import { certificationApi } from "@/features/certification/api/certificationApi";
import { EligibilityPanel } from "@/features/certification/components/EligibilityPanel";
import type {
  Certificate,
  Page,
  Program,
} from "@/features/certification/types/certification.types";
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { organizerError } from "@/features/organization/utils/errorMessage";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
import { useToast } from "@/shared/ui/Toast";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
export default function CertificationProgramsPage() {
  const { organization, grants, setDirty } = useOrganizationContext();
  const [programs, setPrograms] = useState<Program[]>([]);
  const [selected, setSelected] = useState<Program | null>(null);
  const [versions, setVersions] = useState<
    {
      id: string;
      label: string;
    }[]
  >([]);
  const [certificates, setCertificates] = useState<Page<Certificate> | null>(
    null,
  );
  const [tab, setTab] = useState("programs");
  const [name, setName] = useState("");
  const [versionId, setVersionId] = useState("");
  const [status, setStatus] = useState("");
  const [learner, setLearner] = useState("");
  const [learnerDraft, setLearnerDraft] = useState("");
  const [page, setPage] = useState(0);
  const [refresh, setRefresh] = useState(0);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const { toast } = useToast();
  useEffect(() => {
    setDirty("program", Boolean(name || versionId));
    return () => setDirty("program", false);
  }, [name, versionId, setDirty]);
  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    (async () => {
      if (!organization || !grants.includes("ISSUE_CERTIFICATES")) return;
      const rows = await certificationApi.programs(organization.id);
      if (active) {
        setPrograms(rows);
        setSelected((p) => rows.find((r) => r.id === p?.id) ?? rows[0] ?? null);
      }
      const available = await certificationApi.sources(organization.id);
      if (active)
        setVersions(
          available.map((v) => ({
            id: v.id,
            label: `${v.title} · version ${v.versionNo}`,
          })),
        );
    })()
      .catch((e) => {
        if (active) setError(e.message);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [organization, grants, refresh]);
  useEffect(() => {
    let active = true;
    setCertificates(null);
    if (organization && grants.includes("ISSUE_CERTIFICATES"))
      certificationApi
        .search(organization.id, status, "", page, learner)
        .then((r) => {
          if (active) setCertificates(r);
        })
        .catch((e) => {
          if (active) setError(e.message);
        });
    return () => {
      active = false;
    };
  }, [organization, grants, status, learner, page, refresh]);
  async function create() {
    if (!organization || busy || !name.trim() || !versionId) return;
    setBusy(true);
    setError("");
    try {
      const result = await certificationApi.create(organization.id, {
        name: name.trim(),
        courseVersionId: versionId,
      });
      setName("");
      setVersionId("");
      setSelected(result);
      setRefresh((r) => r + 1);
      toast("success", "Certification program created.");
    } catch (e) {
      setError(organizerError(e, "Create failed"));
    } finally {
      setBusy(false);
    }
  }
  async function retire() {
    if (
      !selected ||
      busy ||
      !window.confirm(
        `Retire ${selected.name}? New issuance will stop. Existing certificates remain verifiable.`,
      )
    )
      return;
    setBusy(true);
    setError("");
    try {
      await certificationApi.retire(selected.id);
      setRefresh((x) => x + 1);
      toast("success", "Program retired.");
    } catch (e) {
      setError(organizerError(e, "Unable to retire program"));
    } finally {
      setBusy(false);
    }
  }
  return (
    <OrganizerSurface
      title="Certifications"
      description="Manage programs, evaluate learning evidence and issue certificates."
      authority="ISSUE_CERTIFICATES"
    >
      <div className="sporg-actions">
        <Button
          variant={tab === "programs" ? "primary" : "outline"}
          onClick={() => setTab("programs")}
        >
          Programs and issuance
        </Button>
        <Button
          variant={tab === "certificates" ? "primary" : "outline"}
          onClick={() => setTab("certificates")}
        >
          Issued certificates
        </Button>
      </div>
      {error && (
        <div className="sporg-alert" role="alert">
          {error}
          <Button variant="outline" onClick={() => setRefresh((r) => r + 1)}>
            Retry
          </Button>
        </div>
      )}
      {loading ? (
        <p role="status">Loading programs…</p>
      ) : tab === "programs" ? (
        <div className="sporg-grid">
          <section className="sporg-card">
            <h2>Programs</h2>
            {!programs.length && (
              <p>No programs yet. Publish a course to begin.</p>
            )}
            {programs.map((p) => (
              <div className="sporg-row" key={p.id}>
                <div>
                  <strong>{p.name}</strong>
                  <small>{p.status}</small>
                </div>
                <Button
                  disabled={busy}
                  variant={selected?.id === p.id ? "primary" : "outline"}
                  onClick={() => setSelected(p)}
                >
                  Open
                </Button>
              </div>
            ))}
            <h3>Create program</h3>
            {
              <form
                className="sporg-form"
                onSubmit={(e) => {
                  e.preventDefault();
                  void create();
                }}
              >
                <Input
                  label="Program name"
                  required
                  maxLength={180}
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                />
                <label>
                  Published course version
                  <select
                    required
                    value={versionId}
                    onChange={(e) => setVersionId(e.target.value)}
                  >
                    <option value="">Select a published version</option>
                    {versions.map((v) => (
                      <option key={v.id} value={v.id}>
                        {v.label}
                      </option>
                    ))}
                  </select>
                </label>
                <Button type="submit" disabled={busy || !versionId}>
                  {busy ? "Creating…" : "Create program"}
                </Button>
              </form>
            }
          </section>
          {selected ? (
            <div>
              {selected.status === "ACTIVE" ? (
                <>
                  <EligibilityPanel
                    key={selected.id}
                    program={selected}
                    onIssued={() => setRefresh((r) => r + 1)}
                  />
                  <Button
                    variant="destructive"
                    disabled={busy}
                    onClick={() => void retire()}
                  >
                    Retire program
                  </Button>
                </>
              ) : (
                <section className="sporg-card">
                  <h2>{selected.name}</h2>
                  <p>
                    This program is {selected.status.toLowerCase()}. Issuance
                    requires an active program. Existing certificates remain in
                    the certificate register and can still be verified or
                    revoked.
                  </p>
                </section>
              )}
            </div>
          ) : (
            <section className="sporg-card">
              <h2>Ready to issue?</h2>
              <p>
                Select a program to find learners and evaluate their evidence.
              </p>
            </section>
          )}
        </div>
      ) : (
        <section className="sporg-card">
          <h2>Certificate register</h2>
          <div className="sporg-fields">
            <label>
              Status
              <select
                value={status}
                onChange={(e) => {
                  setStatus(e.target.value);
                  setPage(0);
                }}
              >
                <option value="">All statuses</option>
                <option value="ISSUED">Issued</option>
                <option value="REVOKED">Revoked</option>
              </select>
            </label>
            <Input
              label="Search learner email, program or serial number"
              value={learnerDraft}
              onChange={(e) => setLearnerDraft(e.target.value)}
            />
            <Button
              variant="outline"
              onClick={() => {
                setError("");
                setLearner(learnerDraft.trim());
                setPage(0);
              }}
            >
              Search certificates
            </Button>
          </div>
          {!certificates ? (
            <p role="status">Loading certificates…</p>
          ) : (
            <>
              <p>{certificates.totalElements} certificates</p>
              {certificates.content.map((c) => (
                <div className="sporg-row" key={c.id}>
                  <div>
                    <strong>{c.learnerEmail ?? "Legacy learner record"}</strong>
                    <small>
                      {c.programName ?? "Program"} · {c.serialNumber} ·{" "}
                      {c.status}
                    </small>
                  </div>
                  <Button asChild variant="outline">
                    <Link to={`/organizer/certificates/${c.id}`}>
                      View detail
                    </Link>
                  </Button>
                </div>
              ))}
              {!certificates.content.length && (
                <p>No certificates match these filters.</p>
              )}
              <div className="sporg-actions">
                <Button
                  variant="outline"
                  disabled={page === 0}
                  onClick={() => setPage((p) => p - 1)}
                >
                  Previous
                </Button>
                <span>
                  Page {page + 1} / {Math.max(1, certificates.totalPages)}
                </span>
                <Button
                  variant="outline"
                  disabled={certificates.last}
                  onClick={() => setPage((p) => p + 1)}
                >
                  Next
                </Button>
              </div>
            </>
          )}
        </section>
      )}
    </OrganizerSurface>
  );
}
