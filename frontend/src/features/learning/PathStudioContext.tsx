import { createContext, useContext, type ReactNode } from "react"
import { usePathStudio } from "./usePathStudio"

export type PathStudioState = ReturnType<typeof usePathStudio>

const PathStudioContext = createContext<PathStudioState | null>(null)

export function PathStudioProvider({
  value,
  children,
}: {
  value: PathStudioState
  children: ReactNode
}) {
  return (
    <PathStudioContext.Provider value={value}>
      {children}
    </PathStudioContext.Provider>
  )
}

export function useStudioState() {
  const context = useContext(PathStudioContext)
  if (!context) throw new Error("Path Studio context is required")
  return context
}
