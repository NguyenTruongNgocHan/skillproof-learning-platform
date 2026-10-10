import { learningApi } from "@/features/learning/api/learningApi"
import type { Module, Path, Version } from "@/features/learning/types/learning.types"
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider"
import { organizerError } from "@/features/organization/utils/errorMessage"
import { quizApi } from "@/features/quiz/api/quizApi"
import type {
  Assessment,
  AssessmentQuestion,
  Bank,
  Question,
} from "@/features/quiz/types/quiz.types"
import { useCallback, useEffect, useState } from "react"
import { useParams } from "react-router-dom"

const errorText = (error: unknown) => organizerError(error, "The action could not be saved")

export function usePathStudioData() {
  const { id } = useParams()
  const { organization } = useOrganizationContext()
  const [path, setPath] = useState<Path | null>(null)
  const [versions, setVersions] = useState<Version[]>([])
  const [version, setVersion] = useState<Version | null>(null)
  const [modules, setModules] = useState<Module[]>([])
  const [assessments, setAssessments] = useState<Assessment[]>([])
  const [assessmentItems, setAssessmentItems] = useState<Record<string, AssessmentQuestion[]>>({})
  const [banks, setBanks] = useState<Bank[]>([])
  const [bank, setBank] = useState("")
  const [questions, setQuestions] = useState<Question[]>([])
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(true)
  const [requireResources, setRequireResources] = useState(true)
  const [requireOfficial, setRequireOfficial] = useState(true)

  const load = useCallback(
    async (requestedVersionId?: string) => {
      if (!id) return false
      setLoading(true)
      try {
        if (!organization) throw new Error("Select an approved organization.")
        const owned = await learningApi.owned(organization.id)
        if (!owned.some((candidate) => candidate.id === id)) {
          throw new Error(
            "This path belongs to another organization. Switch organization or return to the path list.",
          )
        }
        const availableVersions = await learningApi.versions(id)
        setPath(owned.find((candidate) => candidate.id === id) ?? null)
        setVersions(availableVersions)
        const chosen =
          availableVersions.find((candidate) => candidate.id === requestedVersionId) ??
          availableVersions.find((candidate) => candidate.status === "DRAFT") ??
          availableVersions.find((candidate) => candidate.status === "PUBLISHED") ??
          availableVersions[0] ??
          null
        setVersion(chosen)
        if (chosen) {
          const [outline, tests] = await Promise.all([
            learningApi.outline(chosen.id),
            quizApi.assessments(chosen.id),
          ])
          setModules(outline.modules)
          setAssessments(tests)
          setRequireResources(outline.policy.require_all_resources)
          setRequireOfficial(outline.policy.require_official_assessments)
          setAssessmentItems(
            Object.fromEntries(
              await Promise.all(
                tests.map(
                  async (test) => [test.id, await quizApi.assessmentQuestions(test.id)] as const,
                ),
              ),
            ),
          )
          const availableBanks = await quizApi.banks(organization.id)
          setBanks(availableBanks)
          setBank((current) => current || availableBanks[0]?.id || "")
        }
        setError("")
        return true
      } catch (error) {
        setError(errorText(error))
        return false
      } finally {
        setLoading(false)
      }
    },
    [id, organization],
  )

  useEffect(() => {
    void load()
  }, [load])

  useEffect(() => {
    let live = true
    if (bank) {
      quizApi
        .questions(bank)
        .then((rows) => {
          if (live) setQuestions(rows)
        })
        .catch((error) => {
          if (live) setError(errorText(error))
        })
    } else {
      setQuestions([])
    }
    return () => {
      live = false
    }
  }, [bank])

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
    loading,
    requireResources,
    requireOfficial,
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
    setRequireResources,
    setRequireOfficial,
  }
}
