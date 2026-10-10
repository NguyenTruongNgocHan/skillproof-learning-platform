import Input from "@/shared/ui/Input"
import type { OrganizationApplication } from "../types/organization.types"
import { applicationFields } from "../validation/organizationApplication"

export function OrganizationApplicationFields({
  form,
  fieldErrors,
  disabled,
  onChange,
}: {
  form: OrganizationApplication
  fieldErrors: Record<string, string>
  disabled: boolean
  onChange: (key: keyof OrganizationApplication, value: string) => void
}) {
  return (
    <div className="sporg-fields">
      {applicationFields.map((field) => (
        <Input
          key={field.key}
          id={`organization-${field.key}`}
          label={field.label}
          required={field.required}
          type={field.type}
          maxLength={field.maxLength}
          error={fieldErrors[field.key]}
          disabled={disabled}
          value={form[field.key] ?? ""}
          onChange={(event) => onChange(field.key, event.target.value)}
        />
      ))}
    </div>
  )
}
