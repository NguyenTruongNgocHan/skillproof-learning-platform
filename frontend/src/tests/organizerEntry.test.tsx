import { afterEach, describe, expect, it, vi } from "vitest"
import { cleanup, render, screen, fireEvent } from "@testing-library/react"
import { MemoryRouter, Route, Routes } from "react-router-dom"
import { OrganizerVerificationGuard } from "@/app/guards/OrganizerVerificationGuard"
const fixture = vi.hoisted(() => ({
  organization: null as unknown,
  pending: null as unknown,
  loading: false,
  error: null as string | null,
  refresh: vi.fn(),
}))
vi.mock("@/features/auth/hooks/useAuth", () => ({
  useAuth: () => ({ user: { id: "organizer-1", role: "ORGANIZER" } }),
}))
vi.mock("@/app/providers/OrganizationProvider", () => ({
  useOrganizationContext: () => fixture,
}))
function open() {
  return render(
    <MemoryRouter initialEntries={["/organizer"]}>
      <Routes>
        <Route
          path="/organizer"
          element={
            <OrganizerVerificationGuard>
              <h1>Workspace ready</h1>
            </OrganizerVerificationGuard>
          }
        />
        <Route
          path="/onboarding/organizer"
          element={<h1>Create your organization</h1>}
        />
        <Route
          path="/organizer/verification-pending"
          element={<h1>Application status</h1>}
        />
      </Routes>
    </MemoryRouter>,
  )
}
afterEach(() => {
  cleanup()
  fixture.organization = null
  fixture.pending = null
  fixture.loading = false
  fixture.error = null
  vi.clearAllMocks()
})
describe("Organizer entry", () => {
  it("first login with no application opens onboarding", () => {
    open()
    expect(screen.getByText("Create your organization")).toBeTruthy()
  })
  it("draft application resumes onboarding", () => {
    fixture.pending = { id: "org", status: "DRAFT" }
    open()
    expect(screen.getByText("Create your organization")).toBeTruthy()
  })
  it("pending application opens its status screen", () => {
    fixture.pending = { id: "org", status: "PENDING" }
    open()
    expect(screen.getByText("Application status")).toBeTruthy()
  })
  it("approved membership opens workspace despite another pending application", () => {
    fixture.organization = { id: "approved", status: "APPROVED" }
    fixture.pending = { id: "owned", status: "PENDING" }
    open()
    expect(screen.getByText("Workspace ready")).toBeTruthy()
  })
  it("API failure is not interpreted as missing application", () => {
    fixture.error = "Organization service unavailable"
    open()
    expect(screen.getByRole("alert")).toHaveTextContent(fixture.error)
    expect(screen.queryByText("Create your organization")).toBeNull()
    fireEvent.click(screen.getByText("Retry"))
    expect(fixture.refresh).toHaveBeenCalledOnce()
  })
  it("waits for context before routing", () => {
    fixture.loading = true
    open()
    expect(screen.getByText("Checking organization access…")).toBeTruthy()
    expect(screen.queryByText("Create your organization")).toBeNull()
  })
})
