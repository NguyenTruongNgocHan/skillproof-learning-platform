import { RealtimeConnection, realtimeApi } from "@/features/realtime/api/realtimeApi"
import type { BattleRating, RealtimeEvent } from "@/features/realtime/types/realtime.types"
import LearnerShell from "@/shared/components/layout/LearnerShell"
import Button from "@/shared/ui/Button"
import { useCallback, useEffect, useRef, useState } from "react"
interface BattleView {
  battleId?: string
  state?: string
  result?: unknown
}
interface BattleQuestion {
  question_version_id?: string
  questionVersionId?: string
  stem?: string
  options?: Array<{
    id: string
    body?: string
    text?: string
  }>
}
export default function ChallengePage() {
  const [connected, setConnected] = useState(false)
  const [skillContext, setSkillContext] = useState("general")
  const [roomCode, setRoomCode] = useState("")
  const [battle, setBattle] = useState<BattleView | null>(null)
  const [question, setQuestion] = useState<BattleQuestion | null>(null)
  const [ratings, setRatings] = useState<BattleRating[]>([])
  const [error, setError] = useState("")
  const connectionRef = useRef<RealtimeConnection | null>(null)

  const loadRatings = useCallback(async () => {
    try {
      setRatings(await realtimeApi.getRatings())
    } catch {
      // Realtime play remains available when rating history cannot be loaded.
    }
  }, [])
  const handleRealtimeEvent = useCallback(
    (event: RealtimeEvent) => {
      const payload = event.payload as BattleView & {
        to?: string
        question?: BattleQuestion
        code?: string
        detail?: string
      }
      if (event.type === "battle.snapshot") setBattle(payload)
      if (event.type === "match.found") {
        setBattle((current) => ({
          ...current,
          battleId: event.battleId,
          state: "MATCHED",
        }))
      }
      if (event.type === "battle.state.changed") {
        setBattle((current) => ({
          ...current,
          battleId: event.battleId,
          state: payload.to,
        }))
      }
      if (event.type === "battle.question.opened") setQuestion(payload.question ?? null)
      if (event.type === "battle.finished") {
        setBattle((current) => ({
          ...current,
          state: "FINISHED",
          result: payload,
        }))
        setQuestion(null)
        void loadRatings()
      }
      if (event.type === "room.created") setRoomCode(payload.code ?? "")
      if (event.type === "error") setError(payload.detail ?? payload.code ?? "Realtime error")
    },
    [loadRatings],
  )
  useEffect(() => {
    const connection = new RealtimeConnection(handleRealtimeEvent, setConnected)
    connectionRef.current = connection
    void connection.connect().catch((cause) => setError(errorMessage(cause)))
    void loadRatings()
    return () => connection.close()
  }, [handleRealtimeEvent, loadRatings])
  function send(type: string, payload: Record<string, unknown> = {}) {
    try {
      connectionRef.current?.send(type, payload)
      setError("")
    } catch (cause) {
      setError(errorMessage(cause))
    }
  }
  const battleId = battle?.battleId
  return (
    <LearnerShell>
      <div className="learner-page learner-page--challenge">
        <header className="learner-page__hero challenge-hero">
          <span className="course-hero__context">Live practice</span>
          <h1>Ready for a live challenge?</h1>
          <p>
            Match with another learner in the same skill area. SkillProof keeps the match, timing,
            answers, and result in sync for both of you.
          </p>
          <p className={`challenge-connection${connected ? " is-online" : ""}`}>
            {connected ? "Ready to match" : "Connecting to live challenge…"}
          </p>
        </header>

        {error && (
          <div className="org-error" role="alert">
            {error}
          </div>
        )}

        {!battleId && (
          <section className="challenge-panel">
            <h2>Find an opponent</h2>
            <label>
              Skill/domain context
              <input
                value={skillContext}
                onChange={(event) => setSkillContext(event.target.value)}
              />
            </label>
            <div className="challenge-actions">
              <Button onClick={() => send("queue.join", { skillContext })}>Quick match</Button>
              <Button variant="outline" onClick={() => send("queue.leave")}>
                Leave queue
              </Button>
              <Button variant="outline" onClick={() => send("room.create", { skillContext })}>
                Create private room
              </Button>
            </div>
            <div className="challenge-room">
              <input
                placeholder="Room code"
                value={roomCode}
                onChange={(event) => setRoomCode(event.target.value.toUpperCase())}
              />
              <Button onClick={() => send("room.join", { code: roomCode })}>Join room</Button>
            </div>
          </section>
        )}

        {battleId && (
          <section className="challenge-panel challenge-panel--battle">
            <span className="course-hero__context">
              {battle?.state === "MATCHED"
                ? "Opponent found"
                : battle?.state === "RUNNING"
                  ? "Challenge in progress"
                  : battle?.state === "FINISHED"
                    ? "Challenge complete"
                    : "Preparing challenge"}
            </span>
            {battle?.state === "MATCHED" && (
              <Button onClick={() => send("battle.ready", { battleId })}>I'm ready</Button>
            )}
            {question && (
              <div>
                <h3>{question.stem}</h3>
                <div className="challenge-options">
                  {(question.options ?? []).map((option) => (
                    <Button
                      key={option.id}
                      variant="outline"
                      onClick={() =>
                        send("battle.answer.submit", {
                          battleId,
                          questionId: question.question_version_id ?? question.questionVersionId,
                          submissionId: crypto.randomUUID(),
                          optionId: option.id,
                        })
                      }
                    >
                      {option.body ?? option.text}
                    </Button>
                  ))}
                </div>
              </div>
            )}
            <Button variant="outline" onClick={() => send("battle.snapshot.request", { battleId })}>
              Reconnect match
            </Button>
          </section>
        )}

        <section className="learner-content-section challenge-ratings">
          <div className="learner-content-section__heading">
            <div>
              <h2>Your battle ratings</h2>
              <p>
                Ratings are skill-specific and remain separate from official learning completion.
              </p>
            </div>
          </div>
          {ratings.length > 0 ? (
            ratings.map((rating) => (
              <p key={rating.skill_context}>
                {rating.skill_context}: <strong>{rating.rating_value}</strong> ·{" "}
                {rating.battle_count} valid battles
              </p>
            ))
          ) : (
            <p>No rated battle yet.</p>
          )}
        </section>
      </div>
    </LearnerShell>
  )
}
function errorMessage(cause: unknown): string {
  return cause instanceof Error ? cause.message : "Realtime command failed"
}
