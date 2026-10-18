import { useEffect } from "react";
import Button from "@/shared/ui/Button";
import type { Organization } from "../types/organization.types";
import type { EvidenceDocument } from "../types/governance.types";
import type { MediaItem } from "@/features/media/types/media.types";

export function OrganizationReviewDialog({ organization, evidence, files, busy, error, onClose, onSubmit }: {
  organization: Organization;
  evidence: EvidenceDocument[];
  files: MediaItem[];
  busy: boolean;
  error: string;
  onClose: () => void;
  onSubmit: () => void;
}) {
  useEffect(() => {
    const onKey = (event: KeyboardEvent) => { if (event.key === "Escape" && !busy) onClose(); };
    document.addEventListener("keydown", onKey);
    const oldOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => { document.removeEventListener("keydown", onKey); document.body.style.overflow = oldOverflow; };
  }, [busy, onClose]);
  const fileById = new Map(files.map(file => [file.id, file]));
  return (
    <div className="sporg-modal-backdrop" onMouseDown={event => { if (event.target === event.currentTarget && !busy) onClose(); }}>
      <section className="sporg-modal" role="dialog" aria-modal="true" aria-labelledby="sporg-modal-title">
        <header className="sporg-modal-header">
          <div><p className="sporg-eyebrow">Ready to submit</p><h2 id="sporg-modal-title">Review your application</h2></div>
          <Button variant="ghost" disabled={busy} onClick={onClose}>Close</Button>
        </header>
        <div className="sporg-modal-body">
          <p>Check the saved information and classified evidence. Submitting locks this revision while it is under review.</p>
          <h3>Organization information</h3>
          <dl className="sporg-review-details">
            <dt>Legal name</dt><dd>{organization.legalName}</dd>
            <dt>Display name</dt><dd>{organization.displayName}</dd>
            <dt>Industry / country</dt><dd>{organization.industry} / {organization.country}</dd>
            <dt>Contact</dt><dd>{organization.contactName} · {organization.contactEmail}</dd>
          </dl>
          <h3>Verification evidence ({evidence.length})</h3>
          <ul className="sporg-modal-evidence">
            {evidence.map(doc => {
              const file = fileById.get(doc.mediaId);
              return <li key={doc.id}>
                <span className="sporg-modal-file" title={file?.name ?? doc.title}>{file?.name ?? doc.title}</span>
                <small>{doc.kind.replaceAll("_", " ")}</small>
              </li>;
            })}
          </ul>
          {error && <p className="sporg-review-error" role="alert">{error}</p>}
        </div>
        <footer className="sporg-modal-footer">
          <Button variant="outline" disabled={busy} onClick={onClose}>Back to editing</Button>
          <Button disabled={busy} onClick={onSubmit}>{busy ? "Submitting…" : "Submit for review"}</Button>
        </footer>
      </section>
    </div>
  );
}
