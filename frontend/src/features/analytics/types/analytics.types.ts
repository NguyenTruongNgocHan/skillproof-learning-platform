export interface Analytics {
  funnel: {
    detailViews: number;
    checkouts: number;
    enrollments: number;
    learning: number;
    completions: number;
    basis: string;
  };
  revenue: { paidOrders: number; grossPaidVnd: number };
  start: string;
  end: string;
  metrics: {
    kind: string;
    events: number;
    visitors: number;
    authenticatedAccounts: number;
  }[];
  courses: {
    versionId: string | null;
    title: string;
    kind: string;
    events: number;
  }[];
  sources: { source: string; campaign: string; kind: string; events: number }[];
  daily: { date: string; kind: string; events: number }[];
}
