import { useEffect, useRef, useState } from "react"
import AppShell from "@/components/layout/AppShell"
import Button from "@/components/ui/Button"
import {
  RealtimeConnection,
  realtimeApi,
  type BattleRating,
  type RealtimeEvent,
} from "@/features/realtime/realtimeApi"

interface BattleView {
  battleId?: string
  state?: string
  result?: unknown
}

interface BattleQuestion {
  question_version_id?: string
  questionVersionId?: string
  stem?: string
  options?: Array<{ id: string; body?: string; text?: string }>
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

  useEffect(() => {
    const connection = new RealtimeConnection(handleRealtimeEvent, setConnected)
    connectionRef.current = connection
    void connection.connect().catch(cause => setError(errorMessage(cause)))
    void loadRatings()
    return () => connection.close()
  }, [])

  async function loadRatings() {
    try {
      setRatings(await realtimeApi.getRatings())
    } catch {
      // Realtime play remains available when rating history cannot be loaded.
    }
  }

  function handleRealtimeEvent(event: RealtimeEvent) {
    const payload = event.payload as Record<string, any>
    if (event.type === "battle.snapshot") setBattle(payload)
    if (event.type === "match.found") {
      setBattle(current => ({ ...current, battleId: event.battleId, state: "MATCHED" }))
    }
    if (event.type === "battle.state.changed") {
      setBattle(current => ({ ...current, battleId: event.battleId, state: payload.to }))
    }
    if (event.type === "battle.question.opened") setQuestion(payload.question)
    if (event.type === "battle.finished") {
      setBattle(current => ({ ...current, state: "FINISHED", result: payload }))
      setQuestion(null)
      void loadRatings()
    }
    if (event.type === "room.created") setRoomCode(payload.code)
    if (event.type === "error") setError(payload.detail ?? payload.code ?? "Realtime error")
  }

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
    <AppShell>
      <div className="v7-wrap">
        <header className="v7-hero">
          <span className="org-eyebrow">REALTIME CHALLENGE · SERVER AUTHORITATIVE</span>
          <h1>1v1 Challenge</h1>
          <p>
            Both learners receive the same immutable question versions. The server
            owns lifecycle, time, accepted answers, score and final result.
          </p>
          <p>{connected ? "Connected" : "Connecting…"}</p>
        </header>

        {error && <div className="org-error" role="alert">{error}</div>}

        {!battleId && (
          <section className="v7-card">
            <h2>Find an opponent</h2>
            <label>
              Skill/domain context
              <input
                value={skillContext}
                onChange={event => setSkillContext(event.target.value)}
              />
            </label>
            <div style={{ display: "flex", gap: 12, flexWrap: "wrap", marginTop: 12 }}>
              <Button onClick={() => send("queue.join", { skillContext })}>Quick match</Button>
              <Button variant="outline" onClick={() => send("queue.leave")}>Leave queue</Button>
              <Button variant="outline" onClick={() => send("room.create", { skillContext })}>
                Create private room
              </Button>
            </div>
            <div style={{ display: "flex", gap: 8, marginTop: 12 }}>
              <input
                placeholder="Room code"
                value={roomCode}
                onChange={event => setRoomCode(event.target.value.toUpperCase())}
              />
              <Button onClick={() => send("room.join", { code: roomCode })}>Join room</Button>
            </div>
          </section>
        )}

        {battleId && (
          <section className="v7-card">
            <span className="org-eyebrow">BATTLE {battle?.state}</span>
            <h2>{battleId}</h2>
            {battle?.state === "MATCHED" && (
              <Button onClick={() => send("battle.ready", { battleId })}>I'm ready</Button>
            )}
            {question && (
              <div>
                <h3>{question.stem}</h3>
                <div style={{ display: "grid", gap: 8 }}>
                  {(question.options ?? []).map(option => (
                    <Button
                      key={option.id}
                      variant="outline"
                      onClick={() => send("battle.answer.submit", {
                        battleId,
                        questionId: question.question_version_id ?? question.questionVersionId,
                        submissionId: crypto.randomUUID(),
                        optionId: option.id,
                      })}
                    >
                      {option.body ?? option.text}
                    </Button>
                  ))}
                </div>
              </div>
            )}
            <Button
              variant="outline"
              onClick={() => send("battle.snapshot.request", { battleId })}
            >
              Sync authoritative state
            </Button>
          </section>
        )}

        <section className="v7-card">
          <h2>Your battle ratings</h2>
          {ratings.length > 0
            ? ratings.map(rating => (
                <p key={rating.skill_context}>
                  {rating.skill_context}: <strong>{rating.rating_value}</strong> ·{" "}
                  {rating.battle_count} valid battles
                </p>
              ))
            : <p>No rated battle yet.</p>}
        </section>
      </div>
    </AppShell>
  )
}

function errorMessage(cause: unknown): string {
  return cause instanceof Error ? cause.message : "Realtime command failed"
}
