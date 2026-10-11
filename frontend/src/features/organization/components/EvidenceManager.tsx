import { useCallback, useState, useEffect } from "react";
import MediaPanel from "@/features/media/components/MediaPanel";
import { mediaApi } from "@/features/media/api/mediaApi";
import { governanceApi } from "../api/governanceApi";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
export function EvidenceManager({
  organizationId,
  purpose = "VERIFICATION",
  editable = false,
  admin = false,
  onBusyChange,
}: {
  organizationId: string;
  purpose?: "VERIFICATION" | "ISSUER";
  editable?: boolean;
  admin?: boolean;
  onBusyChange?: (busy: boolean) => void;
}) {
  const remote = useRemote(
    useCallback(() => governanceApi.evidence(organizationId), [organizationId]),
  );
  const files = useRemote(
    useCallback(
      () =>
        mediaApi.list(
          purpose === "ISSUER" ? "ISSUER" : "ORGANIZATION",
          organizationId,
        ),
      [organizationId, purpose],
    ),
  );
  const action = useAction(() => {
    remote.reload();
    files.reload();
  });
  const [uploading, setUploading] = useState(false);
  useEffect(() => {
    onBusyChange?.(uploading || action.busy);
    return () => onBusyChange?.(false);
  }, [uploading, action.busy, onBusyChange]);
  const [form, setForm] = useState({
    mediaId: "",
    kind: purpose === "ISSUER" ? "TRAINING_CAPABILITY" : "LEGAL_IDENTITY",
    title: "",
    expiresAt: "",
  });
  const [reason, setReason] = useState("");
  const docs = remote.data?.filter((d) => d.purpose === purpose) ?? [];
  const active = files.data ?? [];
  return (
    <section className="sporg-card">
      <h2>
        {purpose === "ISSUER"
          ? "Issuer capability evidence"
          : "Verification evidence"}
      </h2>
      <p>
        {purpose === "ISSUER"
          ? "Provide training credentials, expertise and authority to issue certificates."
          : "Required: legal identity and representative authorization. Activity photos support your application but do not replace legal evidence."}
      </p>
      <MediaPanel
        scope={purpose === "ISSUER" ? "ISSUER" : "ORGANIZATION"}
        target={organizationId}
        writable={editable}
        removable={editable && purpose === "VERIFICATION"}
        onBusyChange={setUploading}
        onChanged={files.reload}
      />
      <RemoteState {...remote} retry={remote.reload} />
      <ActionNotice {...action} />
      {editable && (
        <form
          className="sporg-form"
          onSubmit={(e) => {
            e.preventDefault();
            void action.run(async () => {
              await governanceApi.classify(organizationId, {
                ...form,
                purpose,
                expiresAt: form.expiresAt
                  ? new Date(form.expiresAt).toISOString()
                  : null,
              });
              setForm({ ...form, mediaId: "", title: "" });
            }, "Document classified.");
          }}
        >
          <label>
            Uploaded document
            <select
              required
              value={form.mediaId}
              onChange={(e) => setForm({ ...form, mediaId: e.target.value })}
            >
              <option value="">Select an uploaded document</option>
              {active
                .filter((f) => !remote.data?.some((d) => d.mediaId === f.id))
                .map((f) => (
                  <option key={f.id} value={f.id}>
                    {f.name}
                  </option>
                ))}
            </select>
          </label>
          <label>
            Evidence category
            <select
              value={form.kind}
              onChange={(e) => setForm({ ...form, kind: e.target.value })}
            >
              {(purpose === "ISSUER"
                ? ["TRAINING_CAPABILITY", "REPRESENTATIVE", "SUPPORTING"]
                : ["LEGAL_IDENTITY", "REPRESENTATIVE", "SUPPORTING"]
              ).map((k) => (
                <option key={k}>{k}</option>
              ))}
            </select>
          </label>
          <Input
            label="Document title"
            required
            maxLength={180}
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
          />
          <Input
            label="Expiry (optional)"
            type="datetime-local"
            value={form.expiresAt}
            onChange={(e) => setForm({ ...form, expiresAt: e.target.value })}
          />
          <Button
            type="submit"
            disabled={action.busy || uploading || !form.mediaId}
          >
            Classify uploaded document
          </Button>
        </form>
      )}
      {docs.length === 0 && <p>No classified evidence yet.</p>}
      {docs.map((d) => (
        <div key={d.id} className="sporg-evidence-row">
          <div>
            <strong>{d.title}</strong>
            <small>
              {d.kind} · {d.status.toLowerCase()}{" "}
              {d.expiresAt
                ? `· expires ${new Date(d.expiresAt).toLocaleDateString()}`
                : ""}
            </small>
            {!active.some((f) => f.id === d.mediaId) &&
              purpose === "VERIFICATION" && (
                <small>Historical attachment — not in the current draft</small>
              )}
            {d.reason && <p>{d.reason}</p>}
          </div>
          <Button
            variant="outline"
            onClick={() =>
              void action.run(
                () =>
                  mediaApi.save({
                    id: d.mediaId,
                    name: d.title,
                    mimeType: "application/octet-stream",
                    size: 0,
                    createdAt: "",
                  }),
                "Document downloaded.",
              )
            }
          >
            Download
          </Button>
          {admin && (
            <div className="sporg-actions">
              {["ACCEPTED", "NEEDS_INFORMATION", "REJECTED"].map((status) => (
                <Button
                  key={status}
                  variant="outline"
                  disabled={action.busy || !reason.trim()}
                  onClick={() =>
                    void action.run(
                      () =>
                        governanceApi.reviewEvidence(organizationId, d.id, {
                          status,
                          reason,
                        }),
                      "Evidence reviewed.",
                    )
                  }
                >
                  {status.replaceAll("_", " ").toLowerCase()}
                </Button>
              ))}
            </div>
          )}
        </div>
      ))}
      {admin && (
        <Input
          label="Review reason (required for each decision)"
          required
          value={reason}
          maxLength={1000}
          onChange={(e) => setReason(e.target.value)}
        />
      )}
    </section>
  );
}
