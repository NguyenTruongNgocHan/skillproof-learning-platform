import { useCallback, useEffect, useState } from "react";
import MediaPanel from "@/features/media/components/MediaPanel";
import { mediaApi } from "@/features/media/api/mediaApi";
import { brandingApi, publicImage } from "../api/brandingApi";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
export function OrganizationBrandingPanel({
  organizationId,
}: {
  organizationId: string;
}) {
  const [revision, setRevision] = useState(0),
    [logo, setLogo] = useState(""),
    [cover, setCover] = useState("");
  const remote = useRemote(
    useCallback(() => brandingApi.get(organizationId), [organizationId]),
  );
  const files = useRemote(
    useCallback(() => mediaApi.list("BRAND", organizationId), [organizationId]),
  );
  const action = useAction(() => {
    remote.reload();
    setRevision((x) => x + 1);
  });
  useEffect(() => {
    if (remote.data) {
      setLogo(remote.data.logoMediaId ?? "");
      setCover(remote.data.coverMediaId ?? "");
    }
  }, [remote.data]);
  return (
    <section className="sporg-card">
      <h2>Organization branding</h2>
      <p>
        Logo and cover are public marketing images. Legal evidence stays
        private. JPEG, PNG or WebP, up to 10 MB.
      </p>
      <MediaPanel
        scope="BRAND"
        target={organizationId}
        writable
        onChanged={files.reload}
      />
      <RemoteState {...remote} retry={remote.reload} />
      <ActionNotice {...action} />
      <form
        className="sporg-form"
        onSubmit={(e) => {
          e.preventDefault();
          void action.run(
            () =>
              brandingApi.save(organizationId, {
                logoMediaId: logo,
                coverMediaId: cover,
              }),
            "Branding updated.",
          );
        }}
      >
        {[
          { label: "Logo", value: logo, set: setLogo },
          { label: "Cover", value: cover, set: setCover },
        ].map((field) => (
          <label key={field.label}>
            {field.label}
            <select
              value={field.value}
              onChange={(e) => field.set(e.target.value)}
            >
              <option value="">No image</option>
              {files.data?.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name}
                </option>
              ))}
            </select>
          </label>
        ))}
        <Button type="submit" disabled={action.busy}>
          Apply branding
        </Button>
      </form>
      {remote.data?.coverMediaId && (
        <img
          className="sporg-brand-preview"
          src={publicImage(
            `/public/organizations/${organizationId}/branding/cover?v=${revision}`,
          )}
          alt="Current organization cover"
        />
      )}
    </section>
  );
}
