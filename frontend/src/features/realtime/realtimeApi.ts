import { apiClient } from "@/services/api/apiClient"

export type BattleState =
  | "WAITING"
  | "MATCHED"
  | "READY"
  | "COUNTDOWN"
  | "RUNNING"
  | "FINISHED"

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

export const realtimeApi = {
  issueTicket: () => apiClient.post<WebSocketTicket>("/realtime/ws-ticket"),
  getRatings: () => apiClient.get<BattleRating[]>("/realtime/ratings"),
  getLeaderboard: (skillId: string) =>
    apiClient.get<BattleRating[]>(
      `/realtime/leaderboard?skillId=${encodeURIComponent(skillId)}`,
    ),
}

export class RealtimeConnection {
  private socket: WebSocket | null = null

  constructor(
    private readonly onEvent: (event: RealtimeEvent) => void,
    private readonly onConnectionChange: (connected: boolean) => void,
  ) {}

  async connect(): Promise<void> {
    const ticket = await realtimeApi.issueTicket()
    this.socket = new WebSocket(
      `${ticket.webSocketUrl}?ticket=${encodeURIComponent(ticket.ticket)}`,
    )
    this.socket.onopen = () => this.onConnectionChange(true)
    this.socket.onclose = () => this.onConnectionChange(false)
    this.socket.onmessage = event => {
      this.onEvent(JSON.parse(event.data) as RealtimeEvent)
    }
  }

  send(type: string, payload: Record<string, unknown> = {}): void {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      throw new Error("Realtime connection is not ready")
    }
    this.socket.send(
      JSON.stringify({
        type,
        requestId: crypto.randomUUID(),
        sentAt: new Date().toISOString(),
        payload,
      }),
    )
  }

  close(): void {
    this.socket?.close()
    this.socket = null
  }
}
