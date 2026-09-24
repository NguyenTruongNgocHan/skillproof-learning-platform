export const LANDING_NAV = [
  { label: "How it works", id: "how-it-works" },
  { label: "Learning", id: "learning" },
  { label: "Credentials", id: "credentials" },
  { label: "For organizations", id: "organizations" },
] as const;
export function sectionUrl(id: string) {
  return `/#${id}`;
}
