import { useNavigate } from "react-router-dom"
import { useAuth } from "@/features/auth/hooks/useAuth"
import { useOrganizationContext } from "../providers/OrganizationProvider"
import { governanceApi } from "../api/governanceApi"
import { useAction } from "@/shared/hooks/useRemote"
import { ActionNotice } from "@/shared/ui/RemoteState"
import Button from "@/shared/ui/Button"
export function LegalReverificationPanel() {
  const { user } = useAuth()
  const { organization, refresh, confirmDiscard } = useOrganizationContext()
  const navigate = useNavigate()
  const action = useAction()
  if (!organization || organization.ownerUserId !== user?.id || organization.status !== "APPROVED")
    return null
  return (
    <section className="sporg-card">
      <h2>Change legal identity</h2>
      <p>
        Legal name, registration and country require a new verification application. Opening it
        withdraws current approval and pauses functions that require an approved organization until
        the new application is approved.
      </p>
      <ActionNotice {...action} />
      <Button
        variant="outline"
        disabled={action.busy}
        onClick={() => {
          if (
            confirmDiscard() &&
            window.confirm(
              "Withdraw current organization approval and open a legal reverification draft?",
            )
          )
            void action.run(async () => {
              await governanceApi.legalReverification(organization.id)
              await refresh()
              navigate("/onboarding/organizer")
            }, "Legal verification draft opened.")
        }}
      >
        Open legal reverification
      </Button>
    </section>
  )
}
