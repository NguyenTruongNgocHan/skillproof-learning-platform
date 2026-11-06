export interface Account {
  id: string
  email: string
  displayName: string
  role: "LEARNER" | "ORGANIZER" | "ADMIN"
  createdAt?: string | null
  lastLoginAt?: string | null
  status: "ACTIVE" | "DISABLED" | "PENDING_VERIFICATION" | "LOCKED"
}

export interface AccountPage {
  content: Account[]
  totalElements: number
}
