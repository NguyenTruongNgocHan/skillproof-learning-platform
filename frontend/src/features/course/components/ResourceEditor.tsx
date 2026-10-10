import { useState } from "react";
import { useAction } from "@/shared/hooks/useRemote";
import WorkspaceDialog from "@/features/organization/components/WorkspaceDialog";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
import { ActionNotice } from "@/shared/ui/RemoteState";
import { courseApi } from "../api/courseApi";
import type {
  CourseResource,
  ResourceInput,
  ResourceKind,
} from "../types/course.types";
export function ResourceEditor({
  lessonId,
  position,
  value,
  close,
  refresh,
}: {
  lessonId: string;
  position: number;
  value?: CourseResource;
  close: () => void;
  refresh: () => void;
}) {
  const action = useAction(refresh);
  const [form, setForm] = useState<ResourceInput>({
    position,
    kind: value?.kind ?? "ARTICLE",
    title: value?.title ?? "",
    body: value?.body ?? "",
    url: value?.url ?? "",
    required: value?.required ?? true,
    preview: value?.preview ?? false,
  });
  return (
    <WorkspaceDialog
      title={value ? "Edit resource" : "Add resource"}
      busy={action.busy}
      close={() => {
        if (window.confirm("Discard resource changes?")) close();
      }}
    >
      <form
        className="sporg-form"
        onSubmit={(e) => {
          e.preventDefault();
          void action.run(async () => {
            const input = {
              ...form,
              body: form.body || null,
              url: form.url || null,
            };
            if (value) await courseApi.editResource(value.id, input);
            else await courseApi.resource(lessonId, input);
            close();
          });
        }}
      >
        <Input
          label="Title"
          required
          maxLength={180}
          value={form.title}
          onChange={(e) => setForm({ ...form, title: e.target.value })}
        />
        <div className="sporg-fields">
          <label>
            Type
            <select
              value={form.kind}
              onChange={(e) =>
                setForm({ ...form, kind: e.target.value as ResourceKind })
              }
            >
              {["ARTICLE", "LINK", "VIDEO", "FILE", "AUDIO", "IMAGE"].map(
                (k) => (
                  <option key={k}>{k}</option>
                ),
              )}
            </select>
          </label>
          <Input
            label="Position"
            type="number"
            required
            min={1}
            value={form.position}
            onChange={(e) =>
              setForm({ ...form, position: Number(e.target.value) })
            }
          />
        </div>
        {form.kind === "ARTICLE" && (
          <label>
            Article
            <textarea
              required
              maxLength={100000}
              value={form.body ?? ""}
              onChange={(e) => setForm({ ...form, body: e.target.value })}
            />
          </label>
        )}
        {["LINK", "VIDEO"].includes(form.kind) && (
          <Input
            label="HTTPS / HTTP URL"
            type="url"
            required
            maxLength={1000}
            value={form.url ?? ""}
            onChange={(e) => setForm({ ...form, url: e.target.value })}
          />
        )}
        {!value ? (
          <>
            <label className="sporg-checkbox">
              <input
                type="checkbox"
                checked={form.required}
                onChange={(e) =>
                  setForm({ ...form, required: e.target.checked })
                }
              />{" "}
              Required for completion
            </label>
            <label className="sporg-checkbox">
              <input
                type="checkbox"
                checked={form.preview}
                onChange={(e) =>
                  setForm({ ...form, preview: e.target.checked })
                }
              />{" "}
              Allow preview
            </label>
          </>
        ) : (
          <p>
            Required and preview flags are read-only: the current resource
            update API does not modify them.
          </p>
        )}
        {["FILE", "AUDIO", "IMAGE"].includes(form.kind) && (
          <p>
            Save this resource, then open Files to upload the actual attachment
            before publishing.
          </p>
        )}
        <ActionNotice {...action} />
        <Button disabled={action.busy} type="submit">
          {action.busy ? "Saving…" : "Save resource"}
        </Button>
      </form>
    </WorkspaceDialog>
  );
}
