import PageHeader from "@/shared/components/layout/PageHeader"
import type { ComponentProps } from "react"
export default function AdminPageHeader(props: ComponentProps<typeof PageHeader>) {
  return <PageHeader {...props} breadcrumb={{ label: "Workspace", to: "/admin" }} />
}
