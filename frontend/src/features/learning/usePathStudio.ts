import MediaPanel from "@/features/media/MediaPanel"
import { useCallback, useEffect, useState } from "react"
import { Link, useParams } from "react-router-dom"
import AppShell from "@/components/layout/AppShell"
import Button from "@/components/ui/Button"
import {
  learningApi,
  type Module,
  type Path,
  type Version,
  type Resource,
} from "@/features/learning/learningApi"
import {
  quizApi,
  type Assessment,
  type AssessmentQuestion,
  type Bank,
  type Question,
  type Kind,
} from "@/features/quiz/quizApi"
import { organizationApi } from "@/features/organization/organizationApi"
import { selectedOrganizationId } from "@/features/organization/OrganizationSwitcher"

const errorText = (e: unknown) =>
  e instanceof Error ? e.message : "The action could not be saved"

export function usePathStudio() {
  const { id } = useParams()
  const [path, setPath] = useState<Path | null>(null),
    [versions, setVersions] = useState<Version[]>([]),
    [version, setVersion] = useState<Version | null>(null),
    [modules, setModules] = useState<Module[]>([]),
    [assessments, setAssessments] = useState<Assessment[]>([]),
    [assessmentItems, setAssessmentItems] =
      useState<Record<string, AssessmentQuestion[]>>({}),
    [banks, setBanks] = useState<Bank[]>([]),
    [bank, setBank] = useState(""),
    [questions, setQuestions] = useState<Question[]>([]),
    [error, setError] = useState(""),
    [success, setSuccess] = useState(""),
    [loading, setLoading] = useState(true),
    [busy, setBusy] = useState(false)
  const [moduleTitle, setModuleTitle] = useState(""),
    [editingModule, setEditingModule] = useState<Module | null>(null),
    [resourceModule, setResourceModule] = useState(""),
    [editingResource, setEditingResource] = useState<Resource | null>(null),
    [resourceKind, setResourceKind] = useState<Resource["kind"]>("ARTICLE"),
    [resourceTitle, setResourceTitle] = useState(""),
    [resourceBody, setResourceBody] = useState(""),
    [assessmentTitle, setAssessmentTitle] = useState(""),
    [editingAssessment, setEditingAssessment] = useState<Assessment | null>(
      null,
    ),
    [points, setPoints] = useState(1),
    [kind, setKind] = useState<Kind>("PRACTICE"),
    [duration, setDuration] = useState(900),
    [pass, setPass] = useState(70),
    [max, setMax] = useState(3),
    [attachAssessment, setAttachAssessment] = useState(""),
    [attachQuestion, setAttachQuestion] = useState(""),
    [requireResources, setRequireResources] = useState(true),
    [requireOfficial, setRequireOfficial] = useState(true)
  const load = useCallback(async () => {
    if (!id) return false
    setLoading(true)
    try {
      const mine = await organizationApi.mine()
      const selected = selectedOrganizationId()
      const org = selected
        ? (await organizationApi.memberships()).find((item) => item.id === selected) ?? mine
        : mine
      const [v, owned] = await Promise.all([
        learningApi.versions(id),
        learningApi.owned(org.id),
      ])
      setPath(owned.find((p) => p.id === id) ?? null)
      setVersions(v)
      const chosen =
        v.find((x) => x.status === "DRAFT") ??
        v.find((x) => x.status === "PUBLISHED") ??
        v[0] ??
        null
      setVersion(chosen)
      if (chosen) {
        const [outline, tests] = await Promise.all([
          learningApi.outline(chosen.id),
          quizApi.assessments(chosen.id),
        ])
        setModules(outline.modules)
        setAssessments(tests)
        setAssessmentItems(
          Object.fromEntries(
            await Promise.all(
              tests.map(
                async (test) =>
                  [
                    test.id,
                    await quizApi.assessmentQuestions(test.id),
                  ] as const,
              ),
            ),
          ),
        )
        setRequireResources(outline.policy.require_all_resources)
        setRequireOfficial(outline.policy.require_official_assessments)
        const bs = await quizApi.banks(org.id)
        setBanks(bs)
        setBank((b) => b || bs[0]?.id || "")
      }
      setError("")
      return true
    } catch (e) {
      setError(errorText(e))
      return false
    } finally {
      setLoading(false)
    }
  }, [id])
  useEffect(() => {
    void load()
  }, [load])
  useEffect(() => {
    if (bank)
      quizApi
        .questions(bank)
        .then(setQuestions)
        .catch((e) => setError(errorText(e)))
    else setQuestions([])
  }, [bank])
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
              position: modules.length + 1,
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
      position: editingResource?.position ?? group.resources.length + 1,
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
