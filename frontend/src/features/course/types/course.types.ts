export interface CourseRecord {
  id: string;
  slug: string;
  title: string;
  summary: string;
  organization_id?: string;
}
export interface CourseVersion {
  id: string;
  course_id: string;
  version_no: number;
  status: "DRAFT" | "PUBLISHED" | "ARCHIVED";
}
export interface Lesson {
  id: string;
  moduleId: string;
  position: number;
  title: string;
  body: string;
}
export type ResourceKind =
  "ARTICLE" | "LINK" | "VIDEO" | "FILE" | "AUDIO" | "IMAGE";
export interface CourseResource {
  id: string;
  lesson_id: string | null;
  module_id: string;
  position: number;
  kind: ResourceKind;
  title: string;
  body: string | null;
  url: string | null;
  required: boolean;
  preview: boolean;
}
export interface CourseModule {
  id: string;
  version_id: string;
  position: number;
  title: string;
  lessons: Lesson[];
  resources: CourseResource[];
}
export interface CourseOutline {
  version: CourseVersion;
  modules: CourseModule[];
  policy: {
    require_all_resources: boolean;
    require_official_assessments: boolean;
  };
}
export interface ActivityOwner {
  scope: "COURSE" | "MODULE" | "LESSON";
  id: string;
  label: string;
}
export interface ResourceInput {
  position: number;
  kind: ResourceKind;
  title: string;
  body: string | null;
  url: string | null;
  required: boolean;
  preview: boolean;
}
