import { act, cleanup, renderHook, waitFor } from "@testing-library/react"
import { afterEach, describe, expect, it, vi } from "vitest"
import { useRemote, useAction } from "../hooks/useRemote"
afterEach(cleanup)
describe("remote lifecycle", () => {
  it("ignores a late response after the request context changes", async () => {
    let finish!: (value: string) => void
    const old = () =>
      new Promise<string>((resolve) => {
        finish = resolve
      })
    const current = () => Promise.resolve("New organization")
    const { result, rerender } = renderHook(({ loader }) => useRemote(loader), {
      initialProps: { loader: old },
    })
    rerender({ loader: current })
    await waitFor(() => expect(result.current.data).toBe("New organization"))
    await act(async () => finish("Old organization"))
    expect(result.current.data).toBe("New organization")
  })
  it("locks a mutation synchronously against double clicks", async () => {
    let finish!: () => void
    const operation = vi.fn(
      () =>
        new Promise<void>((resolve) => {
          finish = resolve
        }),
    )
    const { result } = renderHook(() => useAction())
    let pending!: Promise<boolean>
    act(() => {
      pending = result.current.run(operation)
      void result.current.run(operation)
    })
    expect(operation).toHaveBeenCalledTimes(1)
    await act(async () => {
      finish()
      await pending
    })
    expect(result.current.busy).toBe(false)
  })
  it("keeps failure available for a deliberate retry", async () => {
    const { result } = renderHook(() => useAction())
    await act(async () => {
      await result.current.run(async () => {
        throw new Error("Conflict")
      })
    })
    expect(result.current.error).toBe("Conflict")
    expect(result.current.success).toBe("")
    await act(async () => {
      await result.current.run(async () => undefined, "Saved")
    })
    expect(result.current.error).toBe("")
    expect(result.current.success).toBe("Saved")
  })
})
