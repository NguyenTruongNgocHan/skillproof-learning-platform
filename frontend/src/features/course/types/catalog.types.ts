export interface CatalogCourse {
  id: string;
  versionId: string;
  versionNo: number;
  organizationId: string | null;
  slug: string;
  title: string;
  summary: string;
  priceVnd: number;
  certificationPriceVnd: number;
  certificationProgramId: string | null;
}
export interface SyllabusModule {
  id: string;
  position: number;
  title: string;
  lessons: { id: string; position: number; title: string }[];
}
