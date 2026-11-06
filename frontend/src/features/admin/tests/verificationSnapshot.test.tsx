import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { MemoryRouter } from "react-router-dom"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import AdminOrganizationReviewPage from "../pages/AdminOrganizationReviewPage"
import type { EvidenceSnapshot } from "@/features/organization/api/governanceApi"
import { setAccessToken } from "@/shared/api/apiClient"
vi.mock("@/shared/components/layout/AppShell", () => ({
  default: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}))
vi.mock("../components/EvidencePreview", () => ({
  default: ({ label }: { label: string }) => <span>{label}</span>,
}))
vi.mock("../components/ReviewChecklist", async () => {
  const { useEffect } = await import("react")
  return {
    default: function Checklist({ onComplete }: { onComplete: (done: boolean) => void }) {
      useEffect(() => onComplete(true), [onComplete])
      return <p>Checklist saved</p>
    },
  }
})
const org = {
  id: "org",
  displayName: "Organization",
  legalName: "Legal",
  country: "VN",
  industry: "Education",
  status: "PENDING",
}
const revision = {
  id: "revision",
  organizationId: "org",
  revisionNo: 2,
  status: "PENDING",
  documentMediaIds: ["i1", "i2", "i3", "legal", "rep"],
  legalName: "Legal",
  submittedAt: "2026-10-01T00:00:00Z",
}
function evidence(status: string, hashes: string[]): EvidenceSnapshot[] {
  return [
    ...hashes.map((sha256, i) => ({
      evidenceId: `e${i}`,
      mediaId: `i${i + 1}`,
      sha256,
      kind: "COMPANY_IMAGE",
      title: `Image ${i}`,
      status,
      expiresAt: null,
      reason: null,
      reviewerId: null,
      reviewedAt: null,
    })),
    ...["LEGAL_IDENTITY", "REPRESENTATIVE"].map((kind, i) => ({
      evidenceId: kind,
      mediaId: i ? "rep" : "legal",
      sha256: kind,
      kind,
      title: kind,
      status,
      expiresAt: null,
      reason: null,
      reviewerId: null,
      reviewedAt: null,
    })),
  ]
}
let snapshots = evidence("UPLOADED", ["a", "b", "c"])
let fetcher: ReturnType<typeof vi.spyOn>
beforeEach(() => {
  snapshots = evidence("UPLOADED", ["a", "b", "c"])
  fetcher = vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
    const path = new URL(String(input)).pathname
    let body: unknown
    if (path.endsWith("/organizations/pending")) body = [org]
    else if (path.endsWith("/application-revisions")) body = [revision]
    else if (path.endsWith("/evidence-snapshot")) body = snapshots
    else throw new Error(`Unexpected request ${path}`)
    return new Response(JSON.stringify(body), { headers: { "Content-Type": "application/json" } })
  })
})
afterEach(() => {
  cleanup()
  vi.restoreAllMocks()
  setAccessToken(null)
})
async function open() {
  render(
    <MemoryRouter>
      <AdminOrganizationReviewPage />
    </MemoryRouter>,
  )
  fireEvent.click(await screen.findByRole("button", { name: "View details" }))
  await screen.findByText("Checklist saved")
  fireEvent.change(screen.getByLabelText("Decision reason"), {
    target: { value: "Reviewed current revision" },
  })
}
describe("verification revision isolation", () => {
  it("reads revision snapshot rather than current evidence decisions", async () => {
    await open()
    expect(screen.getByRole("button", { name: "Approve" })).toBeDisabled()
    expect(
      fetcher.mock.calls.some((call: unknown[]) =>
        String(call[0]).endsWith("/application-revisions/revision/evidence-snapshot"),
      ),
    ).toBe(true)
    expect(
      fetcher.mock.calls.some((call: unknown[]) =>
        String(call[0]).endsWith("/organizations/org/evidence"),
      ),
    ).toBe(false)
  })
  it("does not count three media IDs with duplicate image contents as three images", async () => {
    snapshots = evidence("ACCEPTED", ["same", "same", "same"])
    await open()
    expect(screen.getByRole("button", { name: "Approve" })).toBeDisabled()
  })
  it("allows approval after current-revision decisions and saved checklist are complete", async () => {
    snapshots = evidence("ACCEPTED", ["a", "b", "c"])
    await open()
    await waitFor(() => expect(screen.getByRole("button", { name: "Approve" })).not.toBeDisabled())
  })
  it("blocks expired mandatory evidence", async () => {
    snapshots = evidence("ACCEPTED", ["a", "b", "c"])
    snapshots[3] = { ...snapshots[3], expiresAt: "2020-01-01T00:00:00Z" }
    await open()
    expect(screen.getByRole("button", { name: "Approve" })).toBeDisabled()
  })
})
