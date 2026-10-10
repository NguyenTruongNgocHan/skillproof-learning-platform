import { cleanup, render, screen } from "@testing-library/react"
import { afterEach, describe, expect, it } from "vitest"
import PasswordInput from "../ui/PasswordInput"
import Input from "../ui/Input"
afterEach(cleanup)
describe("Shared form accessibility", () => {
  it("connects each password label to a distinct input", () => {
    render(
      <>
        <PasswordInput label="Password" />
        <PasswordInput label="Confirm password" />
      </>,
    )
    const password = screen.getByLabelText("Password")
    const confirmation = screen.getByLabelText("Confirm password")
    expect(password.id).not.toBe(confirmation.id)
    expect(password).not.toBe(confirmation)
  })
  it("associates inline errors with their input", () => {
    render(<Input label="Contact email" error="Enter a valid email." />)
    const input = screen.getByLabelText("Contact email")
    expect(input).toHaveAttribute("aria-invalid", "true")
    expect(document.getElementById(input.getAttribute("aria-describedby")!)).toHaveTextContent(
      "Enter a valid email.",
    )
  })
})
