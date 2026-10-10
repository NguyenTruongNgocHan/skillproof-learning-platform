import { useState } from "react";
import {
  Plus,
  ChevronUp,
  ChevronDown,
  FileText,
  BookOpen,
  Paperclip,
} from "lucide-react";
import { useAction } from "@/shared/hooks/useRemote";
import { ActionNotice } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
import WorkspaceDialog from "@/features/organization/components/WorkspaceDialog";
import MediaPanel from "@/features/media/components/MediaPanel";
import { courseApi } from "../api/courseApi";
import type {
  CourseOutline,
  CourseModule,
  Lesson,
  CourseResource,
} from "../types/course.types";
import { ResourceEditor } from "./ResourceEditor";
interface Edit {
  kind: "module" | "lesson";
  parent: string;
  id?: string;
  position: number;
  title: string;
  body: string;
}
export function CourseOutlineEditor({
  outline,
  refresh,
}: {
  outline: CourseOutline;
  refresh: () => void;
}) {
  const editable = outline.version.status === "DRAFT";
  const action = useAction(refresh);
  const [edit, setEdit] = useState<Edit | null>(null);
  const [resource, setResource] = useState<{
    lesson: string;
    position: number;
    value?: CourseResource;
  } | null>(null);
  const [media, setMedia] = useState<CourseResource | null>(null);
  const [uploading, setUploading] = useState(false);
  function reorder(
    ids: string[],
    index: number,
    delta: number,
    save: (ids: string[]) => Promise<unknown>,
  ) {
    const next = [...ids];
    [next[index], next[index + delta]] = [next[index + delta], next[index]];
    void action.run(() => save(next), "Order updated.");
  }
  function arrows(
    ids: string[],
    index: number,
    save: (ids: string[]) => Promise<unknown>,
  ) {
    return (
      editable && (
        <span className="sporg-reorder">
          <button
            aria-label="Move up"
            disabled={action.busy || index === 0}
            onClick={() => reorder(ids, index, -1, save)}
          >
            <ChevronUp size={16} />
          </button>
          <button
            aria-label="Move down"
            disabled={action.busy || index === ids.length - 1}
            onClick={() => reorder(ids, index, 1, save)}
          >
            <ChevronDown size={16} />
          </button>
        </span>
      )
    );
  }
  function remove(kind: "module" | "lesson" | "resource", id: string) {
    if (!window.confirm(`Delete this ${kind} and its dependent draft content?`))
      return;
    void action.run(
      () =>
        kind === "module"
          ? courseApi.deleteModule(id)
          : kind === "lesson"
            ? courseApi.deleteLesson(id)
            : courseApi.deleteResource(id),
      "Content deleted.",
    );
  }
  function lessonForm(m: CourseModule, l?: Lesson) {
    setEdit({
      kind: "lesson",
      parent: m.id,
      id: l?.id,
      position: l?.position ?? m.lessons.length + 1,
      title: l?.title ?? "",
      body: l?.body ?? "",
    });
  }
  function resourceRow(
    r: CourseResource,
    lessonResources: CourseResource[],
    index: number,
  ) {
    return (
      <div className="sporg-resource-row" key={r.id}>
        <FileText size={17} />
        <div>
          <strong>{r.title}</strong>
          <small>
            {r.kind.toLowerCase()} · {r.required ? "Required" : "Optional"}
            {r.preview ? " · Preview" : ""}
          </small>
          {r.body && <p className="sporg-excerpt">{r.body}</p>}
          {r.url && (
            <a
              href={/^https?:\/\//i.test(r.url) ? r.url : undefined}
              target="_blank"
              rel="noreferrer"
            >
              Open resource link ↗
            </a>
          )}
        </div>
        <div className="sporg-actions">
          {r.lesson_id &&
            arrows(
              lessonResources.map((x) => x.id),
              index,
              (ids) => courseApi.orderResources(r.lesson_id!, ids),
            )}
          <Button variant="ghost" onClick={() => setMedia(r)}>
            <Paperclip size={15} /> Files
          </Button>
          {editable && (
            <>
              <Button
                variant="ghost"
                disabled={action.busy}
                onClick={() =>
                  setResource({
                    lesson: r.lesson_id ?? "",
                    position: r.position,
                    value: r,
                  })
                }
              >
                Edit
              </Button>
              <Button
                variant="ghost"
                disabled={action.busy}
                onClick={() => remove("resource", r.id)}
              >
                Delete
              </Button>
            </>
          )}
        </div>
      </div>
    );
  }
  return (
    <>
      <ActionNotice {...action} />
      <div className="sporg-toolbar">
        <h2>Course outline</h2>
        {editable && (
          <Button
            disabled={action.busy}
            variant="outline"
            onClick={() =>
              setEdit({
                kind: "module",
                parent: outline.version.id,
                position: outline.modules.length + 1,
                title: "",
                body: "",
              })
            }
          >
            <Plus size={16} /> Add module
          </Button>
        )}
      </div>
      {outline.modules.length === 0 && (
        <section className="sporg-empty">
          <BookOpen size={32} />
          <h3>Build your course structure</h3>
          <p>
            Add a module, then lessons with articles, links or uploaded media.
          </p>
        </section>
      )}
      {outline.modules.map((m, mi) => (
        <section className="sporg-card sporg-outline-module" key={m.id}>
          <header className="sporg-toolbar">
            <div>
              <small>Module {mi + 1}</small>
              <h2>{m.title}</h2>
            </div>
            <div className="sporg-actions">
              {arrows(
                outline.modules.map((x) => x.id),
                mi,
                (ids) => courseApi.orderModules(outline.version.id, ids),
              )}
              {editable && (
                <>
                  <Button
                    variant="ghost"
                    disabled={action.busy}
                    onClick={() =>
                      setEdit({
                        kind: "module",
                        parent: outline.version.id,
                        id: m.id,
                        position: m.position,
                        title: m.title,
                        body: "",
                      })
                    }
                  >
                    Edit
                  </Button>
                  <Button
                    variant="ghost"
                    disabled={action.busy}
                    onClick={() => remove("module", m.id)}
                  >
                    Delete
                  </Button>
                  <Button
                    variant="outline"
                    disabled={action.busy}
                    onClick={() => lessonForm(m)}
                  >
                    Add lesson
                  </Button>
                </>
              )}
            </div>
          </header>
          {m.lessons.length === 0 && <p>No lessons yet.</p>}
          {m.lessons.map((l, li) => {
            const rows = m.resources.filter((r) => r.lesson_id === l.id);
            return (
              <details className="sporg-lesson" key={l.id} open>
                <summary>
                  {l.title}
                  <small>{rows.length} resources</small>
                </summary>
                <div className="sporg-toolbar">
                  <p className="sporg-lesson-body">
                    {l.body || "No lesson introduction."}
                  </p>
                  <div className="sporg-actions">
                    {arrows(
                      m.lessons.map((x) => x.id),
                      li,
                      (ids) => courseApi.orderLessons(m.id, ids),
                    )}
                    {editable && (
                      <>
                        <Button
                          variant="ghost"
                          disabled={action.busy}
                          onClick={() => lessonForm(m, l)}
                        >
                          Edit lesson
                        </Button>
                        <Button
                          variant="ghost"
                          disabled={action.busy}
                          onClick={() => remove("lesson", l.id)}
                        >
                          Delete
                        </Button>
                        <Button
                          variant="outline"
                          disabled={action.busy}
                          onClick={() =>
                            setResource({
                              lesson: l.id,
                              position: rows.length + 1,
                            })
                          }
                        >
                          Add resource
                        </Button>
                      </>
                    )}
                  </div>
                </div>
                {rows.map((r, i) => resourceRow(r, rows, i))}
              </details>
            );
          })}
          {m.resources
            .filter((r) => !r.lesson_id)
            .map((r, i) => resourceRow(r, [], i))}
        </section>
      ))}
      {edit && (
        <WorkspaceDialog
          title={edit.id ? `Edit ${edit.kind}` : `Add ${edit.kind}`}
          busy={action.busy}
          close={() => {
            if (window.confirm("Discard this editor?")) setEdit(null);
          }}
        >
          <form
            className="sporg-form"
            onSubmit={(e) => {
              e.preventDefault();
              void action.run(async () => {
                const input = {
                  title: edit.title,
                  position: edit.position,
                  body: edit.body,
                };
                if (edit.kind === "module") {
                  if (edit.id) await courseApi.editModule(edit.id, input);
                  else await courseApi.module(edit.parent, input);
                } else {
                  if (edit.id) await courseApi.editLesson(edit.id, input);
                  else await courseApi.lesson(edit.parent, input);
                }
                setEdit(null);
              });
            }}
          >
            <Input
              label="Title"
              required
              maxLength={180}
              value={edit.title}
              onChange={(e) => setEdit({ ...edit, title: e.target.value })}
            />
            <Input
              label="Position"
              type="number"
              required
              min={1}
              value={edit.position}
              onChange={(e) =>
                setEdit({ ...edit, position: Number(e.target.value) })
              }
            />
            {edit.kind === "lesson" && (
              <label>
                Lesson introduction
                <textarea
                  maxLength={100000}
                  value={edit.body}
                  onChange={(e) => setEdit({ ...edit, body: e.target.value })}
                />
              </label>
            )}
            <ActionNotice {...action} />
            <Button type="submit" disabled={action.busy}>
              {action.busy ? "Saving…" : "Save"}
            </Button>
          </form>
        </WorkspaceDialog>
      )}
      {resource && (
        <ResourceEditor
          lessonId={resource.lesson}
          position={resource.position}
          value={resource.value}
          close={() => setResource(null)}
          refresh={refresh}
        />
      )}
      {media && (
        <WorkspaceDialog
          title={`${media.title} · files`}
          busy={uploading}
          close={() => setMedia(null)}
        >
          <MediaPanel
            scope="RESOURCE"
            target={media.id}
            writable={editable}
            removable={editable}
            onBusyChange={setUploading}
          />
        </WorkspaceDialog>
      )}
    </>
  );
}
