import { useCallback } from "react"
import { governanceApi } from "../api/governanceApi"
import { useRemote } from "@/shared/hooks/useRemote"
import { RemoteState } from "@/shared/ui/RemoteState"
export function RevisionEvidencePanel({
  revisionId,
  organizationId,
}: {
  revisionId: string
  organizationId: string
}) {
  const remote = useRemote(
    useCallback(
      () => governanceApi.verificationSnapshot(revisionId, organizationId),
      [revisionId, organizationId],
    ),
  )
  return (
    <section>
      <h3>Evidence decisions for this revision</h3>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data?.length === 0 && <p>No classified evidence in this revision.</p>}
      {remote.data?.map((evidence) => (
        <article key={evidence.id} className="sporg-evidence-row">
          <strong>{evidence.title}</strong>
          <p>
            {evidence.kind} · {evidence.status}
          </p>
          {evidence.reason && <p>{evidence.reason}</p>}
          <small>
            {evidence.expiresAt
              ? `Expires ${new Date(evidence.expiresAt).toLocaleString()}`
              : "No expiry recorded"}
          </small>
        </article>
      ))}
    </section>
  )
}
