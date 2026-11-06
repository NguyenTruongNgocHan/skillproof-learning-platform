import { render, screen, fireEvent, cleanup } from "@testing-library/react"
import { afterEach, describe, expect, it, vi } from "vitest"
import AdminTable from "../components/AdminTable"
afterEach(cleanup)
const rows = [
  { id: "a", title: "Alpha", status: "ACTIVE" },
  { id: "b", title: "Beta", status: "CLOSED" },
]
const columns = [{ key: "title", label: "Title", render: (r: (typeof rows)[number]) => r.title }]
describe("table scope", () => {
  it("updates rows when a local filter value changes", () => {
    render(
      <AdminTable
        rows={rows}
        columns={columns}
        searchText={(r) => r.title}
        filters={[{ key: "status", label: "Status", value: (r) => r.status }]}
        onOpen={vi.fn()}
      />,
    )
    const selects = screen.getAllByRole("combobox")
    fireEvent.change(selects[0], { target: { value: "status" } })
    fireEvent.change(screen.getAllByRole("combobox")[1], { target: { value: "CLOSED" } })
    expect(screen.queryByText("Alpha")).not.toBeInTheDocument()
    expect(screen.getByText("Beta")).toBeInTheDocument()
  })
  it("does not offer page-local search or sorting as server-wide operations", () => {
    const next = vi.fn()
    render(
      <AdminTable
        rows={rows}
        columns={columns}
        searchText={(r) => r.title}
        onOpen={vi.fn()}
        serverPage={0}
        serverNext
        serverTotal={45}
        onServerPage={next}
      />,
    )
    expect(screen.queryByRole("textbox")).not.toBeInTheDocument()
    fireEvent.click(screen.getByRole("button", { name: "Next page" }))
    expect(next).toHaveBeenCalledWith(1)
  })
})
