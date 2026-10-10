export interface LearningPath {
  id: string
  title: string
  organization: string
  level: string
  duration: string
  modules: number
  hasCertification: boolean
  enrolledCount: number
  category: string
}

export interface JourneyStep {
  step: number
  title: string
  description: string
}
