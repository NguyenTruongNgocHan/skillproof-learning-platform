export const LANDING_NAV = [
  { label: "How it works", id: "how-it-works" },
  { label: "Learning", id: "learning" },
  { label: "For organizations", id: "organizations" },
  { label: "Credentials", id: "credentials" },
] as const
export function sectionUrl(id: string) {
  return `/#${id}`
}
