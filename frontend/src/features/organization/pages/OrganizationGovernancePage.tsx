import { workspaceCourseApi } from "@/features/course/api/workspaceCourseApi";

import { useCallback, useState, useEffect } from "react";
import { useOrganizationContext } from "../providers/OrganizationProvider";
import { OrganizerSurface } from "../components/OrganizerSurface";
import { EvidenceManager } from "../components/EvidenceManager";
import { governanceApi } from "../api/governanceApi";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
export default function OrganizationGovernancePage() {
  const { organization, setDirty } = useOrganizationContext();
  const org = organization?.id ?? "";
  const applications = useRemote(
    useCallback(() => governanceApi.issuers(org), [org]),
  );
  const evidence = useRemote(
    useCallback(() => governanceApi.evidence(org), [org]),
  );
  const action = useAction(applications.reload);
  const courseChoices = useRemote(
    useCallback(async () => {
      return (await workspaceCourseApi.directory(org)).map((v) => ({
        id: v.id,
        label: `${v.title} · ${v.id}`,
      }));
    }, [org]),
  );
  const [scopeMode, setScopeMode] = useState("ALL_COURSES"),
    [versionIds, setVersionIds] = useState<string[]>([]);
  const [scope, setScope] = useState("");
  const [selected, setSelected] = useState<string[]>([]);
  useEffect(() => {
    setDirty(
      "issuer-application",
      Boolean(scope || selected.length || versionIds.length),
    );
    return () => setDirty("issuer-application", false);
  }, [scope, selected, versionIds, setDirty]);
  return (
    <OrganizerSurface
      title="Verification & issuer"
      description="Organization verification and certificate issuance are separate approvals."
      authority="MANAGE_PROFILE"
    >
      <EvidenceManager organizationId={org} purpose="VERIFICATION" />
      <EvidenceManager organizationId={org} purpose="ISSUER" editable />
      <section className="sporg-card">
        <h2>Apply for issuer authorization</h2>
        <p>
          Describe the disciplines, qualifications and course programs you are
          authorized to certify. Administrators assess this scope before
          granting time-limited issuer authorization.
        </p>
        <RemoteState {...applications} retry={applications.reload} />
        <ActionNotice {...action} />
        <form
          className="sporg-form"
          onSubmit={(e) => {
            e.preventDefault();
            void action.run(async () => {
              await governanceApi.apply(org, {
                scope,
                evidenceIds: selected,
                scopeMode,
                courseVersionIds: scopeMode === "ALL_COURSES" ? [] : versionIds,
              });
              setScope("");
              setSelected([]);
              setVersionIds([]);
              setScopeMode("ALL_COURSES");
            }, "Issuer application submitted.");
          }}
        >
          <label>
            Course scope
            <select
              value={scopeMode}
              onChange={(e) => setScopeMode(e.target.value)}
            >
              <option value="ALL_COURSES">
                All organization courses (including future courses)
              </option>
              <option value="SELECTED_VERSIONS">
                Specific published course versions
              </option>
            </select>
          </label>
          {scopeMode === "SELECTED_VERSIONS" && (
            <>
              <RemoteState {...courseChoices} retry={courseChoices.reload} />
              {courseChoices.data?.map((v) => (
                <label className="sporg-checkbox" key={v.id}>
                  <input
                    type="checkbox"
                    checked={versionIds.includes(v.id)}
                    onChange={(e) =>
                      setVersionIds(
                        e.target.checked
                          ? [...versionIds, v.id]
                          : versionIds.filter((x) => x !== v.id),
                      )
                    }
                  />
                  {v.label}
                </label>
              ))}
            </>
          )}
          <label>
            Requested scope
            <textarea
              required
              maxLength={2000}
              value={scope}
              onChange={(e) => setScope(e.target.value)}
            />
          </label>
          {evidence.data
            ?.filter((d) => d.purpose === "ISSUER")
            .map((d) => (
              <label className="sporg-checkbox" key={d.id}>
                <input
                  type="checkbox"
                  checked={selected.includes(d.id)}
                  onChange={(e) =>
                    setSelected(
                      e.target.checked
                        ? [...selected, d.id]
                        : selected.filter((id) => id !== d.id),
                    )
                  }
                />
                {d.title} · {d.kind}
              </label>
            ))}
          <Button
            type="submit"
            disabled={
              action.busy ||
              selected.length === 0 ||
              (scopeMode === "SELECTED_VERSIONS" && versionIds.length === 0) ||
              applications.data?.some(
                (a) =>
                  a.status === "PENDING" ||
                  (a.status === "AUTHORIZED" &&
                    Boolean(a.validUntil) &&
                    new Date(a.validUntil!).getTime() >
                      Date.now() + 30 * 86400000),
              )
            }
          >
            Submit issuer application
          </Button>
        </form>
      </section>
      <section className="sporg-card">
        <h2>Authorization history</h2>
        <p>
          You may submit renewal during the final 30 days. A pending renewal
          does not interrupt an existing valid authorization; approval replaces
          it atomically.
        </p>
        {applications.data?.map((a) => (
          <div className="sporg-evidence-row" key={a.id}>
            <strong>{a.status}</strong>
            <p>{a.scope}</p>
            <small>
              {a.scopeMode} {a.courseVersionIds}
            </small>
            <p>{a.reason}</p>
            <small>
              {a.validUntil
                ? `Valid until ${new Date(a.validUntil).toLocaleString()}`
                : "Not authorized"}
            </small>
          </div>
        ))}
      </section>
    </OrganizerSurface>
  );
}
