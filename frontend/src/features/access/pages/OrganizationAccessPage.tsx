import { AccessImportPanel } from "../components/AccessImportPanel";
import { exportPagedCsv } from "@/shared/utils/exportPagedCsv";
import { useCallback, useState } from "react";
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { courseApi } from "@/features/course/api/courseApi";
import { libraryApi } from "@/features/library/api/libraryApi";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { ActionNotice, RemoteState } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
import { accessApi } from "../api/accessApi";
import type { AccessGrant, ProductType } from "../types/access.types";
const uuidPattern =
  "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
export default function OrganizationAccessPage() {
  const { organization, grants } = useOrganizationContext();
  const org = organization?.id ?? "";
  const remote = useRemote(
    useCallback(async () => {
      if (!grants.includes("MANAGE_CONTENT"))
        return { courses: [], resources: [] };
      const [courses, resources] = await Promise.all([
        courseApi.list(org),
        libraryApi.list(org),
      ]);
      const versions = await Promise.all(
        courses.map(async (c) =>
          (await courseApi.versions(c.id))
            .filter((v) => v.status === "PUBLISHED")
            .map((v) => ({ id: v.id, label: `${c.title} · v${v.version_no}` })),
        ),
      );
      return {
        courses: versions.flat(),
        resources: resources
          .filter((r) => r.status === "PUBLISHED")
          .map((r) => ({ id: r.id, label: r.title })),
      };
    }, [org, grants]),
  );
  const [page, setPage] = useState(0),
    [search, setSearch] = useState(""),
    [status, setStatus] = useState("");
  const register = useRemote(
    useCallback(
      () => accessApi.list(org, page, search, status),
      [org, page, search, status],
    ),
  );
  const action = useAction(register.reload);
  const [form, setForm] = useState({
    learnerId: "",
    type: "COURSE" as ProductType,
    productId: "",
    expiresAt: "",
  });
  const [created, setCreated] = useState<AccessGrant | null>(null),
    [revokeId, setRevokeId] = useState("");
  const products =
    form.type === "RESOURCE" ? remote.data?.resources : remote.data?.courses;
  return (
    <OrganizerSurface
      title="Learner access"
      description="Grant learning or certification access without requiring learner payment."
      authority="MANAGE_CONTENT"
    >
      <RemoteState {...remote} retry={remote.reload} />
      <ActionNotice {...action} />
      <div className="sporg-grid">
        <section className="sporg-card">
          <h2>Grant access</h2>
          <form
            className="sporg-form"
            onSubmit={(e) => {
              e.preventDefault();
              if (
                !window.confirm(
                  `Grant ${form.type.toLowerCase()} access to learner ${form.learnerId}?`,
                )
              )
                return;
              void action.run(async () => {
                const g = await accessApi.grant(org, {
                  ...form,
                  expiresAt: form.expiresAt
                    ? new Date(form.expiresAt).toISOString()
                    : null,
                });
                setCreated(g);
              }, "Access granted.");
            }}
          >
            <Input
              label="Learner account ID"
              required
              pattern={uuidPattern}
              value={form.learnerId}
              onChange={(e) => setForm({ ...form, learnerId: e.target.value })}
            />
            <p>
              Use the learner's account UUID. This API does not support email
              lookup.
            </p>
            <label>
              Access type
              <select
                value={form.type}
                onChange={(e) =>
                  setForm({
                    ...form,
                    type: e.target.value as ProductType,
                    productId: "",
                  })
                }
              >
                <option value="COURSE">Learning access</option>
                <option value="CERTIFICATION">
                  Certification participation
                </option>
                <option value="RESOURCE">Library resource access</option>
              </select>
            </label>
            <label>
              Product
              <select
                required
                value={form.productId}
                disabled={remote.loading || Boolean(remote.error)}
                onChange={(e) =>
                  setForm({ ...form, productId: e.target.value })
                }
              >
                <option value="">Choose published content</option>
                {products?.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.label}
                  </option>
                ))}
              </select>
            </label>
            <Input
              label="Expires at (optional, your local time)"
              type="datetime-local"
              value={form.expiresAt}
              onChange={(e) => setForm({ ...form, expiresAt: e.target.value })}
            />
            <p>
              Certification access requires an active certification program. It
              does not grant learning access or guarantee issuance.
            </p>
            <Button
              type="submit"
              disabled={
                action.busy ||
                remote.loading ||
                Boolean(remote.error) ||
                !form.productId
              }
            >
              Grant access
            </Button>
          </form>
          {created && (
            <div className="sporg-success">
              <strong>Grant created</strong>
              <p>
                Grant ID: <code>{created.id}</code>
              </p>
              <p>Learner: {created.learnerId}</p>
              <p>Type: {created.productType}</p>
            </div>
          )}
        </section>
        <section className="sporg-card">
          <h2>Revoke a grant</h2>
          <p>
            Revoke a grant below or use its ID. Revocation does not remove
            historical enrollment records.
          </p>
          <form
            className="sporg-form"
            onSubmit={(e) => {
              e.preventDefault();
              if (window.confirm("Revoke this Organization grant?"))
                void action.run(async () => {
                  await accessApi.revoke(org, revokeId);
                  if (created?.id === revokeId) setCreated(null);
                  setRevokeId("");
                }, "Grant revoked.");
            }}
          >
            <Input
              label="Grant ID"
              required
              pattern={uuidPattern}
              value={revokeId}
              onChange={(e) => setRevokeId(e.target.value)}
            />
            <Button type="submit" variant="outline" disabled={action.busy}>
              Revoke access
            </Button>
          </form>
        </section>
      </div>
      <AccessImportPanel
        org={org}
        type={form.type}
        productId={form.productId}
        expiresAt={
          form.expiresAt ? new Date(form.expiresAt).toISOString() : null
        }
        changed={register.reload}
      />
      <section className="sporg-card">
        <h2>Access register</h2>
        <div className="sporg-toolbar">
          <Input
            label="UUID / product / group"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
          />
          <label>
            Status
            <select
              value={status}
              onChange={(e) => {
                setStatus(e.target.value);
                setPage(0);
              }}
            >
              <option value="">All</option>
              {["ACTIVE", "EXPIRED", "REVOKED"].map((v) => (
                <option key={v}>{v}</option>
              ))}
            </select>
          </label>
          <Button
            variant="outline"
            disabled={action.busy}
            onClick={() =>
              void action.run(
                () =>
                  exportPagedCsv(
                    "access-filtered.csv",
                    (p) => accessApi.list(org, p, search, status),
                    (g) => ({ ...g }),
                  ),
                "Filtered grants exported.",
              )
            }
          >
            Export all filtered grants
          </Button>
        </div>
        <RemoteState {...register} retry={register.reload} />
        {register.data?.content.length === 0 && (
          <p>No organization grants yet.</p>
        )}
        <div className="sporg-table-scroll">
          <table className="sporg-table">
            <thead>
              <tr>
                <th>Learner</th>
                <th>Product</th>
                <th>Expiry</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {register.data?.content.map((g) => (
                <tr key={g.id}>
                  <td>
                    <code>{g.learnerId}</code>
                    <small>{g.cohort || "No group label"}</small>
                  </td>
                  <td>
                    {g.productType}
                    <br />
                    <code>{g.productId}</code>
                  </td>
                  <td>
                    {g.expiresAt
                      ? new Date(g.expiresAt).toLocaleString()
                      : "No expiry"}
                  </td>
                  <td>
                    {g.revokedAt
                      ? "Revoked"
                      : g.expiresAt && new Date(g.expiresAt) <= new Date()
                        ? "Expired"
                        : "Active"}
                  </td>
                  <td>
                    <Button
                      variant="outline"
                      disabled={action.busy || Boolean(g.revokedAt)}
                      onClick={() => {
                        if (window.confirm("Revoke this grant?"))
                          void action.run(
                            () => accessApi.revoke(org, g.id),
                            "Grant revoked.",
                          );
                      }}
                    >
                      Revoke
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="sporg-actions">
          <Button
            variant="outline"
            disabled={page === 0 || register.loading}
            onClick={() => setPage(page - 1)}
          >
            Previous
          </Button>
          <span>
            Page {page + 1} · {register.data?.totalElements ?? 0} grants
          </span>
          <Button
            variant="outline"
            disabled={
              !register.data ||
              page + 1 >= register.data.totalPages ||
              register.loading
            }
            onClick={() => setPage(page + 1)}
          >
            Next
          </Button>
        </div>
      </section>
    </OrganizerSurface>
  );
}
