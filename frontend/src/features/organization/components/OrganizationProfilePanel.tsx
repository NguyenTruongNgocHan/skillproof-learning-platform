import { organizationApi } from "@/features/organization/api/organizationApi";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { organizerError } from "@/features/organization/utils/errorMessage";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
import { useToast } from "@/shared/ui/Toast";
import { useEffect, useState } from "react";
export function OrganizationProfilePanel({
  onDirtyChange,
}: { onDirtyChange?: (dirty: boolean) => void } = {}) {
  const { organization, grants, refresh, setDirty } = useOrganizationContext();
  const [form, setForm] = useState({
    displayName: organization?.displayName ?? "",
    website: organization?.website ?? "",
    industry: organization?.industry ?? "",
    contactPhone: organization?.contactPhone ?? "",
  });
  const [dirty, markDirty] = useState(false),
    [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  const { toast } = useToast();
  useEffect(() => {
    onDirtyChange?.(dirty);
    return () => onDirtyChange?.(false);
  }, [dirty, onDirtyChange]);
  useEffect(() => {
    setDirty("organization-profile", dirty);
    return () => setDirty("organization-profile", false);
  }, [dirty, setDirty]);
  useEffect(() => {
    if (!organization) return;
    setForm({
      displayName: organization.displayName,
      website: organization.website ?? "",
      industry: organization.industry,
      contactPhone: organization.contactPhone ?? "",
    });
    markDirty(false);
    setError("");
  }, [organization]);
  if (!organization) return null;
  const canEdit = grants.includes("MANAGE_PROFILE");
  async function save() {
    if (
      !organization ||
      busy ||
      !grants.includes("MANAGE_PROFILE") ||
      organization.status !== "APPROVED"
    )
      return;
    setBusy(true);
    setError("");
    try {
      await organizationApi.update(organization.id, form);
      markDirty(false);
      setDirty("organization-profile", false);
      await refresh();
      toast("success", "Organization profile saved.");
    } catch (e) {
      setError(organizerError(e, "Unable to save profile"));
    } finally {
      setBusy(false);
    }
  }
  return (
    <section className="sporg-card">
      <h2>Organization profile</h2>
      <dl>
        <dt>Legal name</dt>
        <dd>{organization.legalName}</dd>
        <dt>Country</dt>
        <dd>{organization.country}</dd>
        <dt>Registration number</dt>
        <dd>{organization.registrationNumber || "Not provided"}</dd>
        <dt>Verified contact</dt>
        <dd>
          {organization.contactName} · {organization.contactEmail}
        </dd>
        <dt>Status</dt>
        <dd>{organization.status}</dd>
      </dl>
      <p>
        Legal identity and submitted evidence remain in the reviewed
        application. Update public and contact details below.
      </p>
      {error && (
        <p role="alert" className="sporg-alert">
          {error}
        </p>
      )}
      {canEdit ? (
        <form
          className="sporg-form"
          onSubmit={(e) => {
            e.preventDefault();
            void save();
          }}
        >
          <div className="sporg-fields">
            {(Object.keys(form) as (keyof typeof form)[]).map((key) => (
              <Input
                key={key}
                label={
                  {
                    displayName: "Display name",
                    website: "Website",
                    industry: "Industry",
                    contactPhone: "Contact phone",
                  }[key]
                }
                type={
                  key === "website"
                    ? "url"
                    : key === "contactPhone"
                      ? "tel"
                      : "text"
                }
                required={key === "displayName" || key === "industry"}
                maxLength={
                  {
                    displayName: 200,
                    website: 500,
                    industry: 120,
                    contactPhone: 60,
                  }[key]
                }
                disabled={busy}
                value={form[key]}
                onChange={(e) => {
                  setForm((f) => ({ ...f, [key]: e.target.value }));
                  markDirty(true);
                }}
              />
            ))}
          </div>
          <Button type="submit" disabled={busy || !dirty}>
            {busy ? "Saving…" : "Save profile"}
          </Button>
        </form>
      ) : (
        <p>
          You have read access. Ask your manager for profile management
          permission to edit.
        </p>
      )}
    </section>
  );
}
