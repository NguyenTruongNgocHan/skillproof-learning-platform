import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { MemoryRouter } from "react-router-dom"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { CourseSettings } from "../components/CourseSettings"
import type { CourseOutline } from "../types/course.types"
const api = vi.hoisted(() => ({ edit: vi.fn(), policy: vi.fn() }))
vi.mock("../api/courseApi", () => ({ courseApi: api }))
vi.mock("@/features/organization/providers/OrganizationProvider", () => ({
  useOrganizationContext: () => ({ setDirty: vi.fn() }),
}))
const course = { id: "course", slug: "course", title: "Saved title", summary: "Saved summary" }
const outline: CourseOutline = {
  version: { id: "version", course_id: "course", version_no: 1, status: "DRAFT" },
  modules: [],
  policy: { require_all_resources: true, require_official_assessments: false },
  offer: { accessMode: "PUBLIC", priceVnd: 0, certificationPriceVnd: 0 },
}
beforeEach(() => {
  api.edit.mockReset().mockResolvedValue({})
  api.policy.mockReset().mockResolvedValue({})
})
afterEach(() => {
  cleanup()
  vi.restoreAllMocks()
})
function open(value = outline) {
  render(
    <MemoryRouter>
      <CourseSettings course={course} outline={value} refresh={vi.fn()} />
    </MemoryRouter>,
  )
}
describe("settings form safety", () => {
  it("does not allow bypassing required resources", () => {
    open()
    expect(
      screen.getByRole("checkbox", { name: "Required resources must be completed" }),
    ).toBeChecked()
    expect(
      screen.getByRole("checkbox", { name: "Required resources must be completed" }),
    ).toBeDisabled()
  })
  it("does not discard another unsaved settings form without confirmation", () => {
    vi.spyOn(window, "confirm").mockReturnValue(false)
    open()
    fireEvent.change(screen.getByLabelText("Title"), { target: { value: "Unsaved title" } })
    fireEvent.click(screen.getByRole("checkbox", { name: "Require official assessments" }))
    fireEvent.click(screen.getByRole("button", { name: "Save completion policy" }))
    expect(api.policy).not.toHaveBeenCalled()
    expect(screen.getByLabelText("Title")).toHaveValue("Unsaved title")
  })
  it("always sends the frozen resource policy with an official requirement change", async () => {
    open()
    fireEvent.click(screen.getByRole("checkbox", { name: "Require official assessments" }))
    fireEvent.click(screen.getByRole("button", { name: "Save completion policy" }))
    await waitFor(() =>
      expect(api.policy).toHaveBeenCalledWith("version", {
        requireAllResources: true,
        requireOfficialAssessments: true,
      }),
    )
  })
  it("keeps published settings immutable", () => {
    open({ ...outline, version: { ...outline.version, status: "PUBLISHED" } })
    expect(screen.getByLabelText("Title")).toBeDisabled()
    expect(screen.queryByRole("button", { name: "Save details" })).not.toBeInTheDocument()
  })
})
