import type { Organization, OrganizationApplication } from "../types/organization.types"

export const applicationFields = [
  {
    key: "legalName",
    label: "Legal name",
    maxLength: 200,
    required: true,
    type: "text",
  },
  {
    key: "displayName",
    label: "Display name",
    maxLength: 200,
    required: true,
    type: "text",
  },
  {
    key: "industry",
    label: "Industry",
    maxLength: 120,
    required: true,
    type: "text",
  },
  {
    key: "country",
    label: "Country / jurisdiction",
    maxLength: 120,
    required: true,
    type: "text",
  },
  {
    key: "registrationNumber",
    label: "Registration number",
    maxLength: 120,
    required: false,
    type: "text",
  },
  {
    key: "website",
    label: "Website",
    maxLength: 500,
    required: false,
    type: "url",
  },
  {
    key: "contactName",
    label: "Contact person",
    maxLength: 150,
    required: true,
    type: "text",
  },
  {
    key: "contactEmail",
    label: "Contact email",
    maxLength: 320,
    required: true,
    type: "email",
  },
  {
    key: "contactPhone",
    label: "Contact phone",
    maxLength: 60,
    required: false,
    type: "tel",
  },
] as const

export function applicationFromOrganization(
  value: Organization | null,
  email = "",
): OrganizationApplication {
  return Object.fromEntries(
    applicationFields.map(({ key }) => [
      key,
      value?.[key] ?? (key === "contactEmail" ? email : ""),
    ]),
  ) as unknown as OrganizationApplication
}

export function normalizeApplication(input: OrganizationApplication): OrganizationApplication {
  return Object.fromEntries(
    applicationFields.map(({ key }) => [key, (input[key] ?? "").trim()]),
  ) as unknown as OrganizationApplication
}

export function validateApplication(input: OrganizationApplication): Record<string, string> {
  const errors: Record<string, string> = {}
  const value = normalizeApplication(input)
  for (const field of applicationFields) {
    const text = value[field.key] ?? ""
    if (field.required && !text) errors[field.key] = `${field.label} is required.`
    else if (text.length > field.maxLength)
      errors[field.key] = `Use at most ${field.maxLength} characters.`
  }
  if (value.website && !/^https?:\/\/[^\s]+$/i.test(value.website))
    errors.website = "Use an http:// or https:// website address."
  if (value.contactEmail && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.contactEmail))
    errors.contactEmail = "Enter a valid contact email."
  return errors
}
