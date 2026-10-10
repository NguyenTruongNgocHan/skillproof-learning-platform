import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { MemoryRouter, Route, Routes } from "react-router-dom"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import OrganizerOnboardingPage from "../pages/OrganizerOnboardingPage"
import type { Organization } from "../types/organization.types"
const fixture = vi.hoisted(() => ({
  owned: null as Organization | null,
  loading: false,
  contextError: null as string | null,
  refresh: vi.fn(),
  setDirty: vi.fn(),
  select: vi.fn(),
  toast: vi.fn(),
  api: {
    create: vi.fn(),
    saveDraft: vi.fn(),
    resubmit: vi.fn(),
    submit: vi.fn(),
  },
}))
vi.mock("../providers/OrganizationProvider", () => ({
  useOrganizationContext: () => ({
    owned: fixture.owned,
    loading: fixture.loading,
    error: fixture.contextError,
    refresh: fixture.refresh,
    setDirty: fixture.setDirty,
    select: fixture.select,
  }),
}))
vi.mock("../api/organizationApi", () => ({ organizationApi: fixture.api }))
vi.mock("@/features/auth/hooks/useAuth", () => ({
  useAuth: () => ({ user: { id: "owner", email: "owner@example.org" } }),
}))
vi.mock("@/shared/ui/Toast", () => ({
  useToast: () => ({ toast: fixture.toast }),
}))
vi.mock("@/shared/ui/BrandLogo", () => ({
  default: () => <span>SkillProof</span>,
}))
vi.mock("@/shared/ui/ThemeSwitcher", () => ({
  default: () => <span>Theme</span>,
}))
vi.mock("@/features/media/components/MediaPanel", () => ({
  default: ({ onBusyChange }: { onBusyChange?: (busy: boolean) => void }) => (
    <span>
      Supporting files
      {onBusyChange && <button onClick={() => onBusyChange(true)}>Start attachment upload</button>}
    </span>
  ),
}))
vi.mock("../components/ApplicationHistory", () => ({
  ApplicationHistory: () => <span>Submission history</span>,
}))
const org = (status: Organization["status"]): Organization => ({
  id: "owned",
  ownerUserId: "owner",
  legalName: "SkillProof",
  displayName: "SkillProof",
  industry: "Education",
  country: "VN",
  contactName: "Owner",
  contactEmail: "owner@example.org",
  website: null,
  registrationNumber: null,
  contactPhone: null,
  status,
  reviewReason: status === "REJECTED" ? "Provide registration evidence" : null,
  createdAt: "2026-10-01",
  updatedAt: "2026-10-10",
})
function open() {
  return render(
    <MemoryRouter initialEntries={["/onboarding/organizer"]}>
      <Routes>
        <Route path="/onboarding/organizer" element={<OrganizerOnboardingPage />} />
        <Route path="/organizer/verification-pending" element={<p>Pending review screen</p>} />
        <Route path="/organizer/organization" element={<p>Approved profile screen</p>} />
      </Routes>
    </MemoryRouter>,
  )
}
beforeEach(() => {
  fixture.owned = null
  fixture.loading = false
  fixture.contextError = null
  vi.clearAllMocks()
  fixture.refresh.mockResolvedValue(undefined)
  for (const method of Object.values(fixture.api)) method.mockReset().mockResolvedValue(undefined)
})
afterEach(cleanup)
describe("Organization application activity flow", () => {
  it("creates an owned draft before allowing explicit submission", async () => {
    fixture.api.create.mockImplementation(async () => {
      fixture.owned = org("DRAFT")
      return fixture.owned
    })
    open()
    for (const [label, value] of [
      ["Legal name", "SkillProof"],
      ["Display name", "SkillProof"],
      ["Industry", "Education"],
      ["Country / jurisdiction", "VN"],
      ["Contact person", "Owner"],
    ])
      fireEvent.change(screen.getByLabelText(label), { target: { value } })
    fireEvent.click(screen.getByRole("button", { name: "Save draft" }))
    await waitFor(() => expect(fixture.api.create).toHaveBeenCalledOnce())
    expect(fixture.api.submit).not.toHaveBeenCalled()
    await waitFor(() =>
      expect(screen.getByRole("button", { name: "Review saved application" })).not.toBeDisabled(),
    )
    fireEvent.click(screen.getByRole("button", { name: "Review saved application" }))
    fireEvent.click(screen.getByRole("button", { name: "Submit for review" }))
    await waitFor(() => expect(fixture.api.submit).toHaveBeenCalledOnce())
    expect(await screen.findByText("Pending review screen")).toBeInTheDocument()
  })
  it("reopens a rejected application as a draft without silently submitting", async () => {
    fixture.owned = org("REJECTED")
    fixture.api.resubmit.mockImplementation(async () => {
      fixture.owned = { ...org("DRAFT"), updatedAt: "2026-10-11" }
      return fixture.owned
    })
    open()
    expect(screen.getByText(/Provide registration evidence/)).toBeInTheDocument()
    fireEvent.click(screen.getByRole("button", { name: "Open and save new draft" }))
    await waitFor(() => expect(fixture.api.resubmit).toHaveBeenCalledOnce())
    expect(fixture.api.create).not.toHaveBeenCalled()
    expect(fixture.api.submit).not.toHaveBeenCalled()
    await waitFor(() =>
      expect(screen.getByRole("button", { name: "Review saved application" })).not.toBeDisabled(),
    )
  })
  it("invalidates review when the draft is edited", () => {
    fixture.owned = org("DRAFT")
    open()
    fireEvent.click(screen.getByRole("button", { name: "Review saved application" }))
    expect(screen.getByRole("button", { name: "Submit for review" })).toBeInTheDocument()
    fireEvent.change(screen.getByLabelText("Display name"), {
      target: { value: "Changed" },
    })
    expect(screen.queryByRole("button", { name: "Submit for review" })).not.toBeInTheDocument()
    expect(screen.getByRole("button", { name: "Review saved application" })).toBeDisabled()
  })
  it("prevents duplicate saves while the server request is pending", async () => {
    fixture.owned = org("DRAFT")
    let complete!: () => void
    fixture.api.saveDraft.mockImplementation(
      () =>
        new Promise<void>((resolve) => {
          complete = resolve
        }),
    )
    open()
    const button = screen.getByRole("button", { name: "Save draft" })
    fireEvent.click(button)
    fireEvent.click(button)
    expect(fixture.api.saveDraft).toHaveBeenCalledOnce()
    complete()
    await waitFor(() =>
      expect(screen.getByRole("button", { name: "Save draft" })).not.toBeDisabled(),
    )
  })
  it("shows failed saves and keeps the draft available for retry", async () => {
    fixture.owned = org("DRAFT")
    fixture.api.saveDraft.mockRejectedValue(new Error("Access denied"))
    open()
    fireEvent.click(screen.getByRole("button", { name: "Save draft" }))
    expect(await screen.findByRole("alert")).toHaveTextContent("Access denied")
    expect(fixture.api.submit).not.toHaveBeenCalled()
  })
  it("blocks submission while supporting documents are uploading", () => {
    fixture.owned = org("DRAFT")
    open()
    fireEvent.click(screen.getByRole("button", { name: "Review saved application" }))
    fireEvent.click(screen.getByRole("button", { name: "Start attachment upload" }))
    expect(screen.getByRole("button", { name: "Submit for review" })).toBeDisabled()
    expect(fixture.api.submit).not.toHaveBeenCalled()
  })
  it("routes pending applications to the read-only status page", async () => {
    fixture.owned = org("PENDING")
    open()
    expect(await screen.findByText("Pending review screen")).toBeInTheDocument()
    expect(fixture.api.saveDraft).not.toHaveBeenCalled()
  })
})
