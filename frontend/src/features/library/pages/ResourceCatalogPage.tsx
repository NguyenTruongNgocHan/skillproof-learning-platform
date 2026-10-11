import { useCallback, useState } from "react";
import { Link } from "react-router-dom";
import PublicHeader from "@/shared/components/layout/PublicHeader";
import PublicFooter from "@/shared/components/layout/PublicFooter";
import { apiClient } from "@/shared/api/apiClient";
import { useRemote } from "@/shared/hooks/useRemote";
import { RemoteState } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import "@/features/organization/styles/organizer.css";
export default function ResourceCatalogPage() {
  const [page, setPage] = useState(0);
  const remote = useRemote(
    useCallback(
      () =>
        apiClient.get<
          { id: string; title: string; summary: string; priceVnd: number }[]
        >(`/library?page=${page}&size=20`),
      [page],
    ),
  );
  return (
    <>
      <PublicHeader />
      <main className="sporg">
        <h1>Learning resources</h1>
        <p>
          Read articles and access real files shared by verified publishers and
          community contributors.
        </p>
        <RemoteState {...remote} retry={remote.reload} />
        <div className="sporg-grid">
          {remote.data?.map((r) => (
            <article key={r.id} className="sporg-card">
              <h2>{r.title}</h2>
              <p>{r.summary}</p>
              <p>
                {r.priceVnd === 0
                  ? "Free"
                  : `${r.priceVnd.toLocaleString("vi-VN")} VND`}
              </p>
              <Link to={`/resources/${r.id}`}>View resource →</Link>
            </article>
          ))}
        </div>
        {remote.data?.length === 0 && <p>No public resources on this page.</p>}
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
