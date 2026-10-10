export interface Account {
  id: string
  email: string
  displayName: string
  role: "LEARNER" | "ORGANIZER" | "ADMIN"
  status: "ACTIVE" | "DISABLED" | "PENDING_VERIFICATION"
}

export interface AccountPage {
  content: Account[]
  totalElements: number
}
