import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { AssessmentEditor } from "../components/AssessmentEditor"
import { CourseAssessments } from "../components/CourseAssessments"
import type { Assessment } from "../types/quiz.types"
const fixture = vi.hoisted(() => ({ reopen: vi.fn(), status: "PUBLISHED" }))
vi.mock("@/features/organization/providers/OrganizationProvider", () => ({
  useOrganizationContext: () => ({ organization: { id: "org" }, setDirty: vi.fn() }),
}))
vi.mock("../api/quizApi", () => ({
  quizApi: {
    assessmentQuestions: async () => [],
    banks: async () => [],
    questions: async () => [],
    assessments: async () => [
      {
        id: "a",
        title: "Official",
        status: fixture.status,
        kind: "OFFICIAL",
        duration_seconds: 60,
        pass_percent: 60,
        max_attempts: 1,
      },
    ],
    reopen: fixture.reopen,
  },
}))
const assessment: Assessment = {
  id: "a",
  version_id: "v",
  kind: "OFFICIAL",
  title: "Official",
  status: "PUBLISHED",
  duration_seconds: 60,
  pass_percent: 60,
  max_attempts: 1,
}
beforeEach(() => {
  fixture.reopen.mockReset().mockResolvedValue({})
  vi.spyOn(window, "confirm").mockReturnValue(true)
})
afterEach(() => {
  cleanup()
  vi.restoreAllMocks()
})
describe("assessment lifecycle", () => {
  it("keeps a published assessment immutable even if the course is a draft", async () => {
    render(<AssessmentEditor assessment={assessment} owners={[]} editable refresh={vi.fn()} />)
    expect(screen.getByLabelText("Title")).toBeDisabled()
    expect(screen.queryByRole("button", { name: "Save settings" })).not.toBeInTheDocument()
    await waitFor(() => expect(screen.getByText("No questions attached yet.")).toBeInTheDocument())
  })
  it("offers explicit reopen in a draft course and invalidates the parent readiness", async () => {
    const changed = vi.fn()
    render(<CourseAssessments versionId="v" owners={[]} editable onChanged={changed} />)
    fireEvent.click(await screen.findByRole("button", { name: "Reopen assessment" }))
    await waitFor(() => expect(fixture.reopen).toHaveBeenCalledWith("a"))
    expect(changed).toHaveBeenCalled()
  })
  it("does not offer reopen when the parent course is immutable", async () => {
    render(<CourseAssessments versionId="v" owners={[]} editable={false} />)
    await screen.findByText("Official")
    expect(screen.queryByRole("button", { name: "Reopen assessment" })).not.toBeInTheDocument()
  })
})
