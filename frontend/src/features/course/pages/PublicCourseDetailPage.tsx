import { useCallback } from "react";
import { Link, useParams, useNavigate } from "react-router-dom";
import PublicHeader from "@/shared/components/layout/PublicHeader";
import PublicFooter from "@/shared/components/layout/PublicFooter";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import { useMarketingEvent } from "@/features/analytics/hooks/useMarketingEvent";
import { FeedbackPanel } from "@/features/feedback/components/FeedbackPanel";
import Button from "@/shared/ui/Button";
import { catalogApi } from "../api/catalogApi";
import "@/features/organization/styles/organizer.css";
export default function PublicCourseDetailPage() {
  const { id = "" } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const detail = useRemote(useCallback(() => catalogApi.detail(id), [id]));
  const syllabus = useRemote(useCallback(() => catalogApi.syllabus(id), [id]));
  const action = useAction();
  useMarketingEvent(
    detail.data?.organizationId ?? undefined,
    detail.data?.versionId,
    "DETAIL_VIEW",
  );
  return (
    <>
      <PublicHeader />
      <main className="sporg">
        <Link to="/catalog">← Course catalog</Link>
        <RemoteState {...detail} retry={detail.reload} />
        <ActionNotice {...action} />
        {detail.data && (
          <>
            <header className="sporg-heading">
              <h1>{detail.data.title}</h1>
              <p>{detail.data.summary}</p>
              <p>
                Learning:{" "}
                {detail.data.priceVnd === 0
                  ? "Free"
                  : `${detail.data.priceVnd.toLocaleString("vi-VN")} VND`}{" "}
                · Certification participation:{" "}
                {detail.data.certificationProgramId
                  ? `${detail.data.certificationPriceVnd.toLocaleString("vi-VN")} VND`
                  : "Not offered"}
              </p>
              {detail.data.organizationId && (
                <Link to={`/organizations/${detail.data.organizationId}`}>
                  View issuing organization →
                </Link>
              )}
            </header>
            <section className="sporg-card">
              <h2>Course syllabus</h2>
              <RemoteState {...syllabus} retry={syllabus.reload} />
              {syllabus.data?.map((m) => (
                <details key={m.id} open>
                  <summary>
                    {m.position}. {m.title}
                  </summary>
                  <ol>
                    {m.lessons.map((l) => (
                      <li key={l.id}>{l.title}</li>
                    ))}
                  </ol>
                </details>
              ))}
              <p>
                Enrollment is pinned to a course version. Finishing a course
                does not automatically grant a certificate.
              </p>
              {!user ? (
                <Button asChild>
                  <Link to="/login">Sign in to enroll</Link>
                </Button>
              ) : user.role === "LEARNER" ? (
                <Button
                  disabled={action.busy}
                  onClick={() =>
                    void action.run(async () => {
                      const enrollment = await catalogApi.enroll(id);
                      navigate(`/app/learning/${enrollment.id}`);
                    }, "Enrollment ready.")
                  }
                >
                  Enroll with existing entitlement
                </Button>
              ) : (
                <p>Use the organizer studio to preview your own content.</p>
              )}
              {detail.data.priceVnd > 0 && user?.role === "LEARNER" && (
                <Button asChild variant="outline">
                  <Link
                    to={`/checkout?type=COURSE&productId=${detail.data.versionId}`}
                  >
                    Purchase learning access
                  </Link>
                </Button>
              )}
            </section>
            <FeedbackPanel type="COURSE" target={detail.data.versionId} />
          </>
        )}
      </main>
      <PublicFooter />
    </>
  );
}
