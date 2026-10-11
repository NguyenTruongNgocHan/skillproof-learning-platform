import { useCallback, useState } from "react";
import { Link, useParams } from "react-router-dom";
import PublicHeader from "@/shared/components/layout/PublicHeader";
import PublicFooter from "@/shared/components/layout/PublicFooter";
import { apiClient } from "@/shared/api/apiClient";
import { useAuth } from "@/features/auth/hooks/useAuth";
import MediaPanel from "@/features/media/components/MediaPanel";
import { FeedbackPanel } from "@/features/feedback/components/FeedbackPanel";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import type { LibraryResource } from "../types/library.types";
import "@/features/organization/styles/organizer.css";
export default function PublicResourcePage() {
  const { id = "" } = useParams();
  const { user } = useAuth();
  const [content, setContent] = useState<LibraryResource | null>(null);
  const remote = useRemote(
    useCallback(
      () =>
        apiClient.get<{
          id: string;
          title: string;
          summary: string;
          priceVnd: number;
        }>(`/library/${id}`),
      [id],
    ),
  );
  const action = useAction();
  return (
    <>
      <PublicHeader />
      <main className="sporg">
        <Link to="/resources">← Resources</Link>
        <RemoteState {...remote} retry={remote.reload} />
        <ActionNotice {...action} />
        {remote.data && (
          <>
            <header className="sporg-heading">
              <h1>{remote.data.title}</h1>
              <p>{remote.data.summary}</p>
              <p>
                {remote.data.priceVnd === 0
                  ? "Free access"
                  : `${remote.data.priceVnd.toLocaleString("vi-VN")} VND`}
              </p>
            </header>
            {user ? (
              <div className="sporg-actions">
                <Button
                  disabled={action.busy}
                  onClick={() =>
                    void action.run(
                      async () =>
                        setContent(
                          await apiClient.get<LibraryResource>(
                            `/library/${id}/content`,
                          ),
                        ),
                      "Resource opened.",
                    )
                  }
                >
                  Open with existing access
                </Button>
                {remote.data.priceVnd > 0 && (
                  <Button asChild variant="outline">
                    <Link to={`/checkout?type=RESOURCE&productId=${id}`}>
                      Purchase resource
                    </Link>
                  </Button>
                )}
              </div>
            ) : (
              <Button asChild>
                <Link to="/login">Sign in to open</Link>
              </Button>
            )}
            {content && (
              <section className="sporg-card">
                <p className="sporg-lesson-body">{content.body}</p>
                <MediaPanel scope="LIBRARY" target={id} />
              </section>
            )}
            <FeedbackPanel type="RESOURCE" target={id} />
          </>
        )}
      </main>
      <PublicFooter />
    </>
  );
}
