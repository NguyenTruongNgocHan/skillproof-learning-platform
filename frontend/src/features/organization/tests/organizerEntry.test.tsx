import { ThemeProvider } from "@/app/providers/ThemeProvider";
import { OrganizerVerificationGuard } from "@/app/guards/OrganizerVerificationGuard";
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
const fixture = vi.hoisted(() => ({
  organizations: [],
  grants: [],
  select: vi.fn(),
  organization: null as unknown,
  pending: null as unknown,
  loading: false,
  error: null as string | null,
  refresh: vi.fn(),
}));
vi.mock("@/features/auth/hooks/useAuth", () => ({
  useAuth: () => ({ user: { id: "organizer-1", role: "ORGANIZER" } }),
}));
vi.mock("@/features/organization/providers/OrganizationProvider", () => ({
  useOrganizationContext: () => fixture,
}));
function open() {
  return render(
    <ThemeProvider>
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
      </MemoryRouter>
    </ThemeProvider>,
  );
}
beforeEach(() => {
  vi.stubGlobal(
    "matchMedia",
    vi.fn((query: string) => ({
      matches: false,
      media: query,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(() => true),
    })),
  );
});
afterEach(() => {
  cleanup();
  fixture.organization = null;
  fixture.pending = null;
  fixture.loading = false;
  fixture.error = null;
  vi.clearAllMocks();
  vi.unstubAllGlobals();
});
describe("Organizer entry", () => {
  it("first login with no application opens onboarding", () => {
    open();
    expect(screen.getByText("Create your organization")).toBeTruthy();
  });
  it("draft application resumes onboarding", () => {
    fixture.pending = { id: "org", status: "DRAFT" };
    open();
    expect(screen.getByText("Create your organization")).toBeTruthy();
  });
  it("pending application opens its status screen", () => {
    fixture.pending = { id: "org", status: "PENDING" };
    open();
    expect(screen.getByText("Application status")).toBeTruthy();
  });
  it("approved membership opens workspace despite another pending application", () => {
    fixture.organization = { id: "approved", status: "APPROVED" };
    fixture.pending = { id: "owned", status: "PENDING" };
    open();
    expect(screen.getByText("Workspace ready")).toBeTruthy();
  });
  it("API failure is not interpreted as missing application", () => {
    fixture.error = "Organization service unavailable";
    open();
    expect(screen.getByRole("alert")).toHaveTextContent(fixture.error);
    expect(screen.queryByText("Create your organization")).toBeNull();
    expect(
      screen.getByRole("button", { name: "Open navigation" }),
    ).toBeTruthy();
    expect(screen.queryByText("Workspace ready")).toBeNull();
    fireEvent.click(screen.getByRole("button", { name: "Try again" }));
    expect(fixture.refresh).toHaveBeenCalledOnce();
  });
  it("waits for context before routing", () => {
    fixture.loading = true;
    open();
    expect(screen.getByText("Loading organization access…")).toBeTruthy();
    expect(screen.queryByText("Create your organization")).toBeNull();
    expect(
      screen.getByRole("button", { name: "Open navigation" }),
    ).toBeTruthy();
    expect(screen.queryByText("Workspace ready")).toBeNull();
  });
});
