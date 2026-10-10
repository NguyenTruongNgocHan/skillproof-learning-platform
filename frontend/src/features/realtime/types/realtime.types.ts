export type BattleState = "WAITING" | "MATCHED" | "READY" | "COUNTDOWN" | "RUNNING" | "FINISHED"

export interface WebSocketTicket {
  ticket: string
  expiresAt: string
  webSocketUrl: string
}

export interface RealtimeEvent<TPayload = unknown> {
  type: string
  eventId?: string
  battleId?: string
  sequence?: number
  serverTime?: string
  payload: TPayload
}

export interface BattleRating {
  skill_context: string
  rating_value: number
  battle_count: number
  updated_at: string
}
