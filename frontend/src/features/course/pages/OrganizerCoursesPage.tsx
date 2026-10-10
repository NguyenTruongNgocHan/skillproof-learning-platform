import { useCallback, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { BookOpen, Plus, Search } from "lucide-react";
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { useAction, useRemote } from "@/shared/hooks/useRemote";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
import WorkspaceDialog from "@/features/organization/components/WorkspaceDialog";
import { ActionNotice, RemoteState } from "@/shared/ui/RemoteState";
import { courseApi } from "../api/courseApi";
export default function OrganizerCoursesPage() {
  const { organization, grants } = useOrganizationContext();
  const org = organization?.id ?? "";
  const remote = useRemote(
    useCallback(
      () =>
        grants.includes("MANAGE_CONTENT")
          ? courseApi.list(org)
          : Promise.resolve([]),
      [org, grants],
    ),
  );
  const action = useAction(remote.reload);
  const navigate = useNavigate();
  const [search, setSearch] = useState("");
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ slug: "", title: "", summary: "" });
  return (
    <OrganizerSurface
      title="Courses"
      description="Create, organize and publish learning for your organization."
      authority="MANAGE_CONTENT"
    >
      <div className="sporg-toolbar">
        <label className="sporg-search">
          <Search size={16} />
          <input
            aria-label="Search courses"
            placeholder="Search your courses…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </label>
        <Button
          onClick={() => {
            setForm({ slug: "", title: "", summary: "" });
            setOpen(true);
          }}
        >
          <Plus size={16} /> Create course
        </Button>
      </div>
      <RemoteState
        loading={remote.loading}
        error={remote.error}
        retry={remote.reload}
      />
      {!remote.loading && remote.data && (
        <div className="sporg-course-grid">
          {remote.data
            .filter((c) =>
              (c.title + " " + c.summary)
                .toLowerCase()
                .includes(search.toLowerCase()),
            )
            .map((c) => (
              <Link
                className="sporg-course-card"
                to={`/organizer/courses/${c.id}`}
                key={c.id}
              >
                <div
                  className="sporg-cover-placeholder"
                  aria-label="Course thumbnail not provided"
                >
                  <BookOpen size={30} />
                </div>
                <div>
                  <h2>{c.title}</h2>
                  <p>{c.summary}</p>
                  <span className="sporg-tag">Open studio →</span>
                </div>
              </Link>
            ))}
        </div>
      )}
      {remote.data?.length === 0 && (
        <section className="sporg-empty">
          <BookOpen size={36} />
          <h2>Your first course starts here</h2>
          <p>
            Create a draft, add lessons and resources, then publish when it is
            ready.
          </p>
          <Button onClick={() => setOpen(true)}>Create course</Button>
        </section>
      )}
      {remote.data &&
        remote.data.length > 0 &&
        !remote.data.some((c) =>
          (c.title + " " + c.summary)
            .toLowerCase()
            .includes(search.toLowerCase()),
        ) && <p>No courses match your search.</p>}
      {open && (
        <WorkspaceDialog
          title="Create course"
          busy={action.busy}
          close={() => setOpen(false)}
        >
          <form
            className="sporg-form"
            onSubmit={(e) => {
              e.preventDefault();
              void action.run(async () => {
                const c = await courseApi.create(org, form);
                setOpen(false);
                navigate(`/organizer/courses/${c.id}`);
              }, "Course created.");
            }}
          >
            <Input
              label="Course title"
              required
              maxLength={180}
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
            />
            <Input
              label="URL slug"
              required
              pattern="[a-z0-9]+(?:-[a-z0-9]+)*"
              maxLength={100}
              value={form.slug}
              onChange={(e) => setForm({ ...form, slug: e.target.value })}
            />
            <label>
              Summary
              <textarea
                required
                maxLength={1000}
                value={form.summary}
                onChange={(e) => setForm({ ...form, summary: e.target.value })}
              />
            </label>
            <ActionNotice {...action} />
            <Button type="submit" disabled={action.busy}>
              {action.busy ? "Creating…" : "Create draft"}
            </Button>
          </form>
        </WorkspaceDialog>
      )}
    </OrganizerSurface>
  );
}
