import { useAuth } from "@/features/auth/hooks/useAuth"
import { organizationApi } from "@/features/organization/api/organizationApi"
import type { Authority, Organization } from "@/features/organization/types/organization.types"
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from "react"
import { useBlocker } from "react-router-dom"
import { selectOrganizations } from "../utils/organizationSelection"

interface State {
  all: Organization[]
  organization: Organization | null
  grants: Authority[]
  loading: boolean
  error: string | null
  accountId: string | null
}
interface Context extends State {
  organizations: Organization[]
  pending: Organization | null
  owned: Organization | null
  refresh: () => Promise<void>
  select: (id: string) => void
  setDirty: (key: string, dirty: boolean) => void
}
const Store = createContext<Context | null>(null)
const empty: State = {
  all: [],
  organization: null,
  grants: [],
  loading: true,
  error: null,
  accountId: null,
}
const authorities: Authority[] = [
  "MANAGE_PROFILE",
  "MANAGE_MEMBERS",
  "MANAGE_CONTENT",
  "ISSUE_CERTIFICATES",
]
export function OrganizationProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const accountId = user?.id ?? null
  const role = user?.role
  const [state, setState] = useState<State>(empty)
  const generation = useRef(0)
  const dirty = useRef(new Set<string>())
  const account = useRef(user?.id)
  account.current = user?.id
  const key = user ? `skillproof.organizer.organization.${user.id}` : ""
  const load = useCallback(
    async (selected?: string) => {
      const request = ++generation.current
      if (!accountId || role !== "ORGANIZER") {
        dirty.current.clear()
        setState({ ...empty, loading: false, accountId })
        return
      }
      setState((previous) => ({
        ...previous,
        loading: true,
        error: null,
        accountId,
      }))
      try {
        const all = await organizationApi.memberships()
        let saved = selected ?? null
        try {
          saved ??= localStorage.getItem(key)
        } catch {
          /* Browser storage may be unavailable. */
        }
        const { selected: organization } = selectOrganizations(all, accountId, saved)
        const checks = organization
          ? await Promise.all(authorities.map((a) => organizationApi.can(organization.id, a)))
          : []
        if (request !== generation.current || account.current !== accountId) return
        try {
          if (organization) localStorage.setItem(key, organization.id)
          else localStorage.removeItem(key)
        } catch {
          /* Selection remains available in memory. */
        }
        setState({
          all,
          organization,
          grants: authorities.filter((_, i) => checks[i]?.allowed),
          loading: false,
          error: null,
          accountId,
        })
      } catch (e) {
        if (request === generation.current && account.current === accountId)
          setState({
            ...empty,
            loading: false,
            accountId,
            error: e instanceof Error ? e.message : "Unable to load organizations.",
          })
      }
    },
    [accountId, role, key],
  )
  const refresh = useCallback(async () => {
    if (
      dirty.current.size &&
      !window.confirm("Discard unsaved changes and refresh organization data?")
    )
      return
    dirty.current.clear()
    await load()
  }, [load])
  useEffect(() => {
    dirty.current.clear()
    setState(empty)
    void load()
    return () => {
      generation.current += 1 // Invalidate asynchronous requests from this account lifecycle.
    }
  }, [load])
  const select = useCallback(
    (id: string) => {
      if (id === state.organization?.id) return
      if (dirty.current.size && !window.confirm("Discard unsaved changes and switch organization?"))
        return
      dirty.current.clear()
      void load(id)
    },
    [load, state.organization?.id],
  )
  const setDirty = useCallback((id: string, value: boolean) => {
    if (value) dirty.current.add(id)
    else dirty.current.delete(id)
  }, [])
  useEffect(() => {
    const before = (e: BeforeUnloadEvent) => {
      if (dirty.current.size) {
        e.preventDefault()
        e.returnValue = ""
      }
    }
    window.addEventListener("beforeunload", before)
    return () => {
      window.removeEventListener("beforeunload", before)
    }
  }, [])
  const blocker = useBlocker(
    ({ currentLocation, nextLocation }) =>
      dirty.current.size > 0 &&
      currentLocation.pathname + currentLocation.search !==
        nextLocation.pathname + nextLocation.search,
  )
  useEffect(() => {
    if (blocker.state !== "blocked") return
    if (window.confirm("Discard unsaved changes and leave this page?")) {
      dirty.current.clear()
      blocker.proceed()
    } else blocker.reset()
  }, [blocker])
  const current = state.accountId === (user?.id ?? null) ? state : empty
  const value = useMemo(
    () => ({
      ...current,
      organizations: current.all.filter((o) => o.status === "APPROVED"),
      owned: current.all.find((o) => o.ownerUserId === user?.id) ?? null,
      pending:
        current.all.find((o) => o.ownerUserId === user?.id && o.status !== "APPROVED") ?? null,
      refresh,
      select,
      setDirty,
    }),
    [current, user?.id, refresh, select, setDirty],
  )
  return <Store.Provider value={value}>{children}</Store.Provider>
}
export function useOrganizationContext() {
  const value = useContext(Store)
  if (!value) throw new Error("OrganizationProvider is required")
  return value
}
