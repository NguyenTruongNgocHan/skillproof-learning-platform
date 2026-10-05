import MediaPanel from "@/features/media/MediaPanel"
import Button from "@/components/ui/Button"
import { learningApi, type Resource } from "@/features/learning/learningApi"
import { quizApi, type Kind } from "@/features/quiz/quizApi"
import { useStudioState } from "@/features/learning/PathStudioContext"

export default function PublishingSection() {
  const {
    id,
    path,
    version,
    modules,
    assessments,
    busy,
    requireResources,
    requireOfficial,
    act,
    setRequireResources,
    setRequireOfficial,
  } = useStudioState()
  if (!version) return null
  const draft = version.status === "DRAFT"
  return (
    <section className="v7-section">
      <h2>3. Completion and publication</h2>
      <div className="v7-card">
        <p>
          This version is complete when the selected requirements are met.
          Practice and mock scores never substitute for official assessments.
        </p>
        {draft ? (
          <>
            <label className="v7-option">
              <input
                type="checkbox"
                checked={requireResources}
                onChange={(e) => setRequireResources(e.target.checked)}
              />
              Complete all learning resources
            </label>
            <label className="v7-option">
              <input
                type="checkbox"
                checked={requireOfficial}
                onChange={(e) => setRequireOfficial(e.target.checked)}
              />
              Pass every official assessment
            </label>
            {assessments.some((a) => a.status === "DRAFT") && (
              <p>
                Publish or remove every draft assessment before publishing this
                path version.
              </p>
            )}
            <div className="v7-inline">
              <Button
                disabled={busy}
                variant="outline"
                onClick={() =>
                  void act(
                    () =>
                      learningApi.policy(version.id, {
                        requireAllResources: requireResources,
                        requireOfficialAssessments: requireOfficial,
                      }),
                    "Completion policy saved.",
                  )
                }
              >
                Save completion rules
              </Button>
              <Button
                disabled={
                  busy ||
                  modules.length === 0 ||
                  assessments.some((a) => a.status === "DRAFT")
                }
                onClick={() => {
                  if (
                    window.confirm(
                      "Publish this version? The content and assessments will become immutable.",
                    )
                  )
                    void act(async () => {
                      await learningApi.policy(version.id, {
                        requireAllResources: requireResources,
                        requireOfficialAssessments: requireOfficial,
                      })
                      return learningApi.publish(version.id)
                    }, "Learning path published.")
                }}
              >
                Publish version
              </Button>
            </div>
          </>
        ) : (
          <p>
            This published version is fixed. Create a draft to make changes
            without rewriting learner history.
          </p>
        )}
      </div>
    </section>
  )
}
