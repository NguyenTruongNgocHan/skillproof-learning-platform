import { learningApi } from "@/features/learning/api/learningApi"
import { usePathStudioData } from "@/features/learning/hooks/usePathStudioData"
import type { Module, Resource } from "@/features/learning/types/learning.types"
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider"
import { organizerError } from "@/features/organization/utils/errorMessage"
import { quizApi } from "@/features/quiz/api/quizApi"
import type { Assessment, Kind } from "@/features/quiz/types/quiz.types"
import { useEffect, useState } from "react"

const errorText = (error: unknown) => organizerError(error, "The action could not be saved")

export function usePathStudio() {
  const data = usePathStudioData()
  const { setDirty } = useOrganizationContext()
  const {
    id,
    path,
    versions,
    version,
    modules,
    assessments,
    assessmentItems,
    banks,
    bank,
    questions,
    error,
    loading,
    load,
    setPath,
    setVersions,
    setVersion,
    setModules,
    setAssessments,
    setAssessmentItems,
    setBanks,
    setBank,
    setQuestions,
    setError,
    setLoading,
    requireResources,
    requireOfficial,
    setRequireResources,
    setRequireOfficial,
  } = data
  const [success, setSuccess] = useState("")
  const [busy, setBusy] = useState(false)
  const [moduleTitle, setModuleTitle] = useState("")
  const [editingModule, setEditingModule] = useState<Module | null>(null)
  const [resourceModule, setResourceModule] = useState("")
  const [editingResource, setEditingResource] = useState<Resource | null>(null)
  const [resourceKind, setResourceKind] = useState<Resource["kind"]>("ARTICLE")
  const [resourceTitle, setResourceTitle] = useState("")
  const [resourceBody, setResourceBody] = useState("")
  const [assessmentTitle, setAssessmentTitle] = useState("")
  const [editingAssessment, setEditingAssessment] = useState<Assessment | null>(null)
  const [points, setPoints] = useState(1)
  const [kind, setKind] = useState<Kind>("PRACTICE")
  const [duration, setDuration] = useState(900)
  const [pass, setPass] = useState(70)
  const [max, setMax] = useState(3)
  const [attachAssessment, setAttachAssessment] = useState("")
  const [attachQuestion, setAttachQuestion] = useState("")

  useEffect(() => {
    setDirty(
      "path-studio",
      Boolean(moduleTitle || resourceTitle || resourceBody || assessmentTitle),
    )
    return () => setDirty("path-studio", false)
  }, [moduleTitle, resourceTitle, resourceBody, assessmentTitle, setDirty])
  async function act(job: () => Promise<unknown>, message: string) {
    setBusy(true)
    setError("")
    setSuccess("")
    try {
      await job()
      const refreshed = await load()
      if (refreshed) setSuccess(message)
      return refreshed
    } catch (e) {
      setError(errorText(e))
      return false
    } finally {
      setBusy(false)
    }
  }
  async function addModule(e: React.FormEvent) {
    e.preventDefault()
    if (!version) return
    const saved = await act(
      () =>
        editingModule
          ? learningApi.editModule(editingModule.id, {
              title: moduleTitle,
              position: editingModule.position,
            })
          : learningApi.module(version.id, {
              title: moduleTitle,
              position: Math.max(0, ...modules.map((m) => m.position)) + 1,
            }),
      "Module saved.",
    )
    if (saved) {
      setEditingModule(null)
      setModuleTitle("")
    }
  }
  async function addResource(e: React.FormEvent) {
    e.preventDefault()
    const group = modules.find((m) => m.id === resourceModule)
    if (!group) return
    const data = {
      position:
        editingResource?.position ?? Math.max(0, ...group.resources.map((r) => r.position)) + 1,
      kind: resourceKind,
      title: resourceTitle,
      ...(resourceKind === "ARTICLE"
        ? { body: resourceBody, url: null }
        : resourceKind === "FILE" || resourceKind === "AUDIO"
          ? { body: null, url: null }
          : { url: resourceBody, body: null }),
    }
    const saved = await act(
      () =>
        editingResource
          ? learningApi.editResource(editingResource.id, data)
          : learningApi.resource(group.id, data),
      "Resource saved.",
    )
    if (saved) {
      setEditingResource(null)
      setResourceTitle("")
      setResourceBody("")
    }
  }
  async function addAssessment(e: React.FormEvent) {
    e.preventDefault()
    if (!version) return
    const saved = await act(
      () =>
        editingAssessment
          ? quizApi.editAssessment(editingAssessment.id, {
              title: assessmentTitle,
              durationSeconds: duration,
              passPercent: pass,
              maxAttempts: max,
            })
          : quizApi.createAssessment(version.id, {
              title: assessmentTitle,
              kind,
              durationSeconds: duration,
              passPercent: pass,
              maxAttempts: max,
            }),
      "Assessment draft saved.",
    )
    if (saved) {
      setEditingAssessment(null)
      setAssessmentTitle("")
    }
  }
  async function attach(e: React.FormEvent) {
    e.preventDefault()
    const target = assessments.find((a) => a.id === attachAssessment)
    if (!target || !attachQuestion) return
    const rows = assessmentItems[target.id] ?? []
    await act(
      () => quizApi.attach(target.id, attachQuestion, rows.length + 1, points),
      "Question added to assessment.",
    )
  }
  return {
    id,
    path,
    versions,
    version,
    modules,
    assessments,
    assessmentItems,
    banks,
    bank,
    questions,
    error,
    success,
    loading,
    busy,
    moduleTitle,
    editingModule,
    resourceModule,
    editingResource,
    resourceKind,
    resourceTitle,
    resourceBody,
    assessmentTitle,
    editingAssessment,
    points,
    kind,
    duration,
    pass,
    max,
    attachAssessment,
    attachQuestion,
    requireResources,
    requireOfficial,
    load,
    act,
    addModule,
    addResource,
    addAssessment,
    attach,
    setPath,
    setVersions,
    setVersion,
    setModules,
    setAssessments,
    setAssessmentItems,
    setBanks,
    setBank,
    setQuestions,
    setError,
    setSuccess,
    setLoading,
    setBusy,
    setModuleTitle,
    setEditingModule,
    setResourceModule,
    setEditingResource,
    setResourceKind,
    setResourceTitle,
    setResourceBody,
    setAssessmentTitle,
    setEditingAssessment,
    setPoints,
    setKind,
    setDuration,
    setPass,
    setMax,
    setAttachAssessment,
    setAttachQuestion,
    setRequireResources,
    setRequireOfficial,
  }
}
