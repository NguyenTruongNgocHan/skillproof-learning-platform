import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import {
  render,
  screen,
  fireEvent,
  waitFor,
  cleanup,
  act,
} from "@testing-library/react"
import { createMemoryRouter, RouterProvider } from "react-router-dom"
import {
  OrganizationProvider,
  useOrganizationContext,
} from "@/app/providers/OrganizationProvider"
const fixture = vi.hoisted(() => ({ memberships: vi.fn(), can: vi.fn() }))
vi.mock("@/features/auth/hooks/useAuth", () => ({
  useAuth: () => ({ user: { id: "organizer", role: "ORGANIZER" } }),
}))
vi.mock("@/features/organization/organizationApi", () => ({
  organizationApi: fixture,
}))
const organizations = [
  {
    id: "a",
    ownerUserId: "organizer",
    displayName: "Alpha",
    status: "APPROVED",
  },
  { id: "b", ownerUserId: "other", displayName: "Beta", status: "APPROVED" },
  { id: "c", ownerUserId: "other", displayName: "Gamma", status: "APPROVED" },
]
function Probe() {
  const context = useOrganizationContext()
  return (
    <>
      <p data-testid="organization">
        {context.loading
          ? "Loading"
          : (context.organization?.displayName ?? "None")}
      </p>
      <button onClick={() => context.select("b")}>Beta</button>
      <button onClick={() => context.select("c")}>Gamma</button>
      <button onClick={() => context.setDirty("form", true)}>Edit form</button>
    </>
  )
}
function open() {
  const router = createMemoryRouter(
    [
      {
        path: "*",
        element: (
          <OrganizationProvider>
            <Probe />
          </OrganizationProvider>
        ),
      },
    ],
    { initialEntries: ["/previous", "/organizer"], initialIndex: 1 },
  )
  render(<RouterProvider router={router} />)
  return router
}
beforeEach(() => {
  localStorage.clear()
  fixture.memberships.mockReset().mockResolvedValue(organizations)
  fixture.can.mockReset().mockResolvedValue({ allowed: true })
})
afterEach(() => {
  cleanup()
  vi.restoreAllMocks()
})
describe("Organization context", () => {
  it("ignores an invalid saved organization", async () => {
    localStorage.setItem(
      "skillproof.organizer.organization.organizer",
      "removed",
    )
    open()
    await waitFor(() =>
      expect(screen.getByTestId("organization")).toHaveTextContent("Alpha"),
    )
    expect(
      localStorage.getItem("skillproof.organizer.organization.organizer"),
    ).toBe("a")
  })
  it("keeps unsaved work when a switch is cancelled", async () => {
    open()
    await waitFor(() =>
      expect(screen.getByTestId("organization")).toHaveTextContent("Alpha"),
    )
    vi.spyOn(window, "confirm").mockReturnValue(false)
    fireEvent.click(screen.getByText("Edit form"))
    fireEvent.click(screen.getByText("Beta"))
    expect(screen.getByTestId("organization")).toHaveTextContent("Alpha")
    expect(fixture.memberships).toHaveBeenCalledOnce()
  })
  it("ignores an older switching response", async () => {
    open()
    await waitFor(() =>
      expect(screen.getByTestId("organization")).toHaveTextContent("Alpha"),
    )
    let first!: (value: unknown) => void
    let second!: (value: unknown) => void
    fixture.memberships
      .mockReturnValueOnce(new Promise((resolve) => (first = resolve)))
      .mockReturnValueOnce(new Promise((resolve) => (second = resolve)))
    fireEvent.click(screen.getByText("Beta"))
    fireEvent.click(screen.getByText("Gamma"))
    second(organizations)
    await waitFor(() =>
      expect(screen.getByTestId("organization")).toHaveTextContent("Gamma"),
    )
    first(organizations)
    await waitFor(() =>
      expect(
        localStorage.getItem("skillproof.organizer.organization.organizer"),
      ).toBe("c"),
    )
  })
  it("blocks browser history navigation when discard is cancelled", async () => {
    const router = open()
    await waitFor(() =>
      expect(screen.getByTestId("organization")).toHaveTextContent("Alpha"),
    )
    const confirm = vi.spyOn(window, "confirm").mockReturnValue(false)
    fireEvent.click(screen.getByText("Edit form"))
    await act(async () => {
      await router.navigate(-1)
    })
    await waitFor(() => expect(confirm).toHaveBeenCalled())
    expect(router.state.location.pathname).toBe("/organizer")
  })
})
