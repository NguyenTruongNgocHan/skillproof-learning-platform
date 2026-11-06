import { cleanup, fireEvent, render, screen } from "@testing-library/react"
import { afterEach, describe, expect, it, vi } from "vitest"
import Modal from "../ui/Modal"
afterEach(cleanup)
describe("shared dialog", () => {
  it("traps focus, supports escape and restores the invoking control", () => {
    const trigger = document.createElement("button")
    document.body.append(trigger)
    trigger.focus()
    const close = vi.fn()
    const { unmount } = render(
      <Modal open title="Review" onClose={close}>
        <button>Last action</button>
      </Modal>,
    )
    const first = screen.getByRole("button", { name: "Close" })
    const last = screen.getByRole("button", { name: "Last action" })
    expect(first).toHaveFocus()
    fireEvent.keyDown(document, { key: "Tab", shiftKey: true })
    expect(last).toHaveFocus()
    fireEvent.keyDown(document, { key: "Tab" })
    expect(first).toHaveFocus()
    fireEvent.keyDown(document, { key: "Escape" })
    expect(close).toHaveBeenCalledOnce()
    unmount()
    expect(trigger).toHaveFocus()
    trigger.remove()
  })
})
