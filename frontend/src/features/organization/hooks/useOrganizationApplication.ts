import { useAuth } from "@/features/auth/hooks/useAuth"
import { ApiError } from "@/shared/api/apiClient"
import { useToast } from "@/shared/ui/Toast"
import { useEffect, useRef, useState, type FormEvent } from "react"
import { useNavigate } from "react-router-dom"
import { organizationApi } from "../api/organizationApi"
import { useOrganizationContext } from "../providers/OrganizationProvider"
import type { OrganizationApplication } from "../types/organization.types"
import {
  applicationFromOrganization,
  normalizeApplication,
  validateApplication,
} from "../validation/organizationApplication"

export function useOrganizationApplication() {
  const {
    owned,
    loading,
    error: contextError,
    refresh,
    setDirty,
    select,
  } = useOrganizationContext()
  const { user } = useAuth()
  const navigate = useNavigate()
  const { toast } = useToast()
  const [form, setForm] = useState(() => applicationFromOrganization(null, user?.email))
  const [dirty, markDirty] = useState(false)
  const [busy, setBusy] = useState(false)
  const [attachmentsBusy, setAttachmentsBusy] = useState(false)
  const [error, setError] = useState("")
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [review, setReview] = useState(false)
  const inFlight = useRef(false)
  // Hydrate the owned application; the selected workspace never supplies this form.
  useEffect(() => {
    setForm(applicationFromOrganization(owned, user?.email))
    markDirty(false)
    setReview(false)
    setError("")
    setFieldErrors({})
  }, [owned, user?.id, user?.email])
  useEffect(() => {
    setDirty("application", dirty)
    return () => setDirty("application", false)
  }, [dirty, setDirty])
  useEffect(() => {
    if (loading || contextError) return
    if (owned?.status === "PENDING") navigate("/organizer/verification-pending", { replace: true })
    if (owned?.status === "APPROVED") {
      select(owned.id)
      navigate("/organizer/organization", { replace: true })
    }
  }, [owned?.status, owned?.id, loading, contextError, navigate, select])

  const changeField = (key: keyof OrganizationApplication, value: string) => {
    setForm((previous) => ({ ...previous, [key]: value }))
    markDirty(true)
    setReview(false)
    setFieldErrors((previous) => ({ ...previous, [key]: "" }))
  }
  function reportFailure(cause: unknown, fallback: string) {
    setError(cause instanceof Error ? cause.message : fallback)
    if (cause instanceof ApiError) setFieldErrors(cause.problem.fieldErrors ?? {})
  }
  async function save(event: FormEvent) {
    event.preventDefault()
    if (
      inFlight.current ||
      attachmentsBusy ||
      loading ||
      contextError ||
      (owned && !["DRAFT", "REJECTED"].includes(owned.status))
    )
      return
    const errors = validateApplication(form)
    setFieldErrors(errors)
    if (Object.keys(errors).length) return
    inFlight.current = true
    setBusy(true)
    setError("")
    try {
      const input = normalizeApplication(form)
      if (!owned) await organizationApi.create(input)
      else if (owned.status === "REJECTED") await organizationApi.resubmit(input)
      else await organizationApi.saveDraft(input)
      markDirty(false)
      setDirty("application", false)
      setReview(false)
      await refresh()
      toast("success", "Draft saved. Review the saved details and documents before submitting.")
    } catch (cause) {
      reportFailure(cause, "Unable to save application.")
    } finally {
      inFlight.current = false
      setBusy(false)
    }
  }
  async function submit() {
    if (
      inFlight.current ||
      attachmentsBusy ||
      owned?.status !== "DRAFT" ||
      dirty ||
      !review ||
      loading ||
      contextError
    )
      return
    inFlight.current = true
    setBusy(true)
    setError("")
    try {
      await organizationApi.submit()
      await refresh()
      toast("success", "Application submitted for review.")
      navigate("/organizer/verification-pending", { replace: true })
    } catch (cause) {
      reportFailure(cause, "Unable to submit application.")
    } finally {
      inFlight.current = false
      setBusy(false)
    }
  }
  return {
    owned,
    form,
    dirty,
    busy,
    attachmentsBusy,
    setAttachmentsBusy,
    error,
    fieldErrors,
    review,
    setReview,
    loading,
    contextError,
    refresh,
    changeField,
    save,
    submit,
  }
}
