import { useState } from "react";
import { ShieldCheck, Image } from "lucide-react";
import type { Organization } from "../types/organization.types";
// Branding URLs are intentionally optional until a dedicated backend contract exists.
export function OrganizationIdentity({
  organization,
  logoUrl,
  coverUrl,
}: {
  organization: Organization;
  logoUrl?: string;
  coverUrl?: string;
}) {
  const [logoFailed, setLogoFailed] = useState(false),
    [coverFailed, setCoverFailed] = useState(false);
  const initials = organization.displayName
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((s) => s[0])
    .join("")
    .toUpperCase();
  return (
    <section className="sporg-identity">
      <div className="sporg-identity-cover">
        {coverUrl && !coverFailed ? (
          <img src={coverUrl} alt="" onError={() => setCoverFailed(true)} />
        ) : (
          <div className="sporg-cover-label">
            <Image size={18} />
            <span>Organization cover</span>
          </div>
        )}
      </div>
      <div className="sporg-identity-content">
        <div className="sporg-org-avatar">
          {logoUrl && !logoFailed ? (
            <img
              src={logoUrl}
              alt={`${organization.displayName} logo`}
              onError={() => setLogoFailed(true)}
            />
          ) : (
            <span aria-label="Organization initials">{initials}</span>
          )}
        </div>
        <div>
          <h2>{organization.displayName}</h2>
          <p>
            {organization.industry} · {organization.country}
          </p>
          {organization.website && (
            <a
              href={
                /^https?:\/\//i.test(organization.website)
                  ? organization.website
                  : undefined
              }
              target="_blank"
              rel="noreferrer"
            >
              Visit website ↗
            </a>
          )}
        </div>
        <span
          className={`sporg-badge sporg-badge-${organization.status.toLowerCase()}`}
        >
          {organization.status === "APPROVED" && <ShieldCheck size={14} />}{" "}
          {organization.status.toLowerCase()}
        </span>
      </div>
    </section>
  );
}
