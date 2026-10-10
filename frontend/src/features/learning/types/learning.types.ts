export interface Path {
  id: string
  organization_id: string
  slug: string
  title: string
  summary: string
  organization_name?: string
  version_id?: string
  current_version_id?: string
  draft_version_id?: string
  version_no?: number
}

export interface Version {
  id: string
  path_id: string
  version_no: number
  status: "DRAFT" | "PUBLISHED" | "ARCHIVED"
  published_at?: string | null
}

export interface Resource {
  id: string
  module_id: string
  position: number
  kind: "ARTICLE" | "LINK" | "VIDEO" | "FILE" | "AUDIO"
  title: string
  body: string | null
  url: string | null
  completed?: boolean
}

export interface Module {
  id: string
  version_id: string
  position: number
  title: string
  resources: Resource[]
}

export interface Enrollment {
  id: string
  learner_id: string
  path_id: string
  version_id: string
  status: "ACTIVE" | "COMPLETED"
  title: string
  summary: string
  version_no: number
  enrolled_at: string
}

export interface Progress {
  totalResources: number
  completedResources: number
  officialAssessments: number
  passedAssessments: number
  completed: boolean
}

export interface Course {
  enrollment: Enrollment
  modules: Module[]
  progress: Progress
}
