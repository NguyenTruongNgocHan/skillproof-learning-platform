import { useCallback, useState } from "react";
import { Link, useParams } from "react-router-dom";
import PublicHeader from "@/shared/components/layout/PublicHeader";
import PublicFooter from "@/shared/components/layout/PublicFooter";
import { OrganizationIdentity } from "../components/OrganizationIdentity";
import { brandingApi } from "../api/brandingApi";
import { apiClient } from "@/shared/api/apiClient";
import { useRemote } from "@/shared/hooks/useRemote";
import { RemoteState } from "@/shared/ui/RemoteState";
import { useMarketingEvent } from "@/features/analytics/hooks/useMarketingEvent";
import type { CatalogCourse } from "@/features/course/types/catalog.types";
import Button from "@/shared/ui/Button";
import "../styles/organizer.css";
export default function PublicOrganizationPage() {
  const { id = "" } = useParams();
  const [page, setPage] = useState(0);
  const profile = useRemote(
    useCallback(() => brandingApi.publicProfile(id), [id]),
  );
  const courses = useRemote(
    useCallback(
      () =>
        apiClient.get<CatalogCourse[]>(
          `/public/organizations/${encodeURIComponent(id)}/courses?page=${page}`,
        ),
      [id, page],
    ),
  );
  useMarketingEvent(profile.data?.id, undefined, "ORG_VIEW");
  return (
    <>
      <PublicHeader />
      <main className="sporg">
        <Link to="/catalog">← Explore courses</Link>
        <RemoteState {...profile} retry={profile.reload} />
        {profile.data && (
          <>
            <OrganizationIdentity organization={profile.data} />
            <section className="sporg-card">
              <h2>Verified organization</h2>
              <p>{profile.data.legalName}</p>
              <p>
                Organization verification confirms the reviewed application.
                Certificate issuance requires a separate active authorization.
              </p>
            </section>
            <h2>Public courses</h2>
            <RemoteState {...courses} retry={courses.reload} />
            <div className="sporg-grid">
              {courses.data?.map((c) => (
                <article key={c.id} className="sporg-card">
                  <h3>{c.title}</h3>
                  <p>{c.summary}</p>
                  <Link to={`/catalog/${c.id}`}>View course →</Link>
                </article>
              ))}
            </div>
            {courses.data?.length === 0 && (
              <p>No public courses on this page.</p>
            )}
            <div className="sporg-actions">
              <Button disabled={page === 0} onClick={() => setPage(page - 1)}>
                Previous
              </Button>
              <Button
                disabled={!courses.data || courses.data.length < 20}
                onClick={() => setPage(page + 1)}
              >
                Next
              </Button>
            </div>
          </>
        )}
      </main>
      <PublicFooter />
    </>
  );
}
