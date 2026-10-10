import Button from "@/shared/ui/Button"
import { OrganizerDashboardFrame } from "./OrganizerDashboardFrame"
import { OrganizerSurface } from "./OrganizerSurface"

export function OrganizerWorkspaceState({
  loading,
  error,
  retry,
}: {
  loading: boolean
  error: string | null
  retry: () => Promise<void>
}) {
  return (
    <OrganizerSurface
      title="Your organization workspace"
      description="Organization access and reporting workspace."
    >
      <section className="sporg-card sporg-workspace-state" aria-busy={loading}>
        {loading ? (
          <div role="status" aria-live="polite">
            <h2>Loading organization access…</h2>
            <p>Your workspace navigation remains available while we verify organization access.</p>
            <div className="sporg-skeleton" aria-hidden="true" />
            <div className="sporg-skeleton sporg-skeleton-short" aria-hidden="true" />
          </div>
        ) : (
          <div role="alert">
            <h2>Organization data is unavailable</h2>
            <p>{error ?? "Unable to load organization access."}</p>
            <p>Protected content and actions remain unavailable until access can be verified.</p>
            <Button onClick={() => void retry()}>Try again</Button>
          </div>
        )}
      </section>
      <OrganizerDashboardFrame />
    </OrganizerSurface>
  )
}
