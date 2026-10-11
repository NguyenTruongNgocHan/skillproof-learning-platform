import { publicImage } from "@/features/organization/api/brandingApi";
import { useState, useCallback } from "react";
import { Link } from "react-router-dom";
import PublicHeader from "@/shared/components/layout/PublicHeader";
import PublicFooter from "@/shared/components/layout/PublicFooter";
import { useRemote } from "@/shared/hooks/useRemote";
import { RemoteState } from "@/shared/ui/RemoteState";
import { useMarketingEvent } from "@/features/analytics/hooks/useMarketingEvent";
import Button from "@/shared/ui/Button";
import { catalogApi } from "../api/catalogApi";
import type { CatalogCourse } from "../types/catalog.types";
import "@/features/organization/styles/organizer.css";
export default function CourseCatalogPage() {
  const [page, setPage] = useState(0);
  const remote = useRemote(useCallback(() => catalogApi.list(page), [page]));
  return (
    <>
      <PublicHeader />
      <main className="sporg">
        <header className="sporg-heading">
          <h1>Explore courses</h1>
          <p>
            Explore freely. Learning access, certification participation and
            completion requirements are separate.
          </p>
        </header>
        <RemoteState {...remote} retry={remote.reload} />
        <div className="sporg-grid">
          {remote.data?.map((c) => (
            <CatalogCard key={c.id} course={c} />
          ))}
        </div>
        {remote.data?.length === 0 && (
          <p>No public courses available on this page.</p>
        )}
        <div className="sporg-actions">
          <Button disabled={page === 0} onClick={() => setPage(page - 1)}>
            Previous
          </Button>
          <Button onClick={() => setPage(page + 1)}>Next page</Button>
        </div>
      </main>
      <PublicFooter />
    </>
  );
}
function CatalogCard({ course: c }: { course: CatalogCourse }) {
  const impression = useMarketingEvent(
    c.organizationId ?? undefined,
    c.versionId,
    "IMPRESSION",
  );
  return (
    <article ref={impression} className="sporg-card">
      <CourseCover id={c.id} />
      <small>
        v{c.versionNo} ·{" "}
        {c.certificationProgramId
          ? "Certification available"
          : "Learning course"}
      </small>
      <h2>{c.title}</h2>
      <p>{c.summary}</p>
      <p>
        {c.priceVnd === 0
          ? "Free learning"
          : `${c.priceVnd.toLocaleString("vi-VN")} VND`}
      </p>
      <Button asChild variant="outline">
        <Link to={`/catalog/${c.id}`}>View course</Link>
      </Button>
    </article>
  );
}

function CourseCover({ id }: { id: string }) {
  const [failed, setFailed] = useState(false);
  return failed ? (
    <div className="sporg-course-cover-placeholder">Course materials</div>
  ) : (
    <img
      className="sporg-course-cover"
      src={publicImage(`/public/courses/${id}/cover`)}
      alt="Course cover"
      onError={() => setFailed(true)}
    />
  );
}
