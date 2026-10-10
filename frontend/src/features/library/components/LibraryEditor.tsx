import { useState, useEffect } from "react";
import MediaPanel from "@/features/media/components/MediaPanel";
import { useAction } from "@/shared/hooks/useRemote";
import { ActionNotice } from "@/shared/ui/RemoteState";
import Input from "@/shared/ui/Input";
import Button from "@/shared/ui/Button";
import type { LibraryInput, LibraryResource } from "../types/library.types";
import { libraryApi } from "../api/libraryApi";
export function LibraryEditor({
  onBusyChange,
  org,
  value,
  created,
  refresh,
}: {
  onBusyChange?: (busy: boolean) => void;
  org: string;
  value: LibraryResource | null;
  created: (r: LibraryResource) => void;
  refresh: () => void;
}) {
  const [current, setCurrent] = useState(value);
  const [form, setForm] = useState<LibraryInput>({
    title: value?.title ?? "",
    summary: value?.summary ?? "",
    body: value?.body ?? "",
    priceVnd: value?.priceVnd ?? 0,
    accessMode: value?.accessMode ?? "PUBLIC",
  });
  const action = useAction(refresh);

  const [uploading, setUploading] = useState(false);
  useEffect(() => {
    onBusyChange?.(action.busy || uploading);
    return () => onBusyChange?.(false);
  }, [action.busy, uploading, onBusyChange]);
  const editable = !current || ["DRAFT", "REJECTED"].includes(current.status);
  return (
    <>
      <ActionNotice {...action} />
      <form
        className="sporg-form"
        onSubmit={(e) => {
          e.preventDefault();
          void action.run(async () => {
            const result = current
              ? await libraryApi.edit(current.id, form)
              : await libraryApi.create(org, form);
            setCurrent(result);
            created(result);
          }, "Resource saved.");
        }}
      >
        <fieldset disabled={!editable || action.busy || uploading}>
          <Input
            label="Title"
            required
            maxLength={180}
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
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
          <label>
            Article text (or attach a file after saving)
            <textarea
              maxLength={100000}
              value={form.body}
              onChange={(e) => setForm({ ...form, body: e.target.value })}
            />
          </label>
          <div className="sporg-fields">
            <label>
              Access mode
              <select
                disabled={Boolean(current)}
                value={form.accessMode}
                onChange={(e) =>
                  setForm({
                    ...form,
                    accessMode: e.target.value as LibraryInput["accessMode"],
                    priceVnd:
                      e.target.value === "RESTRICTED" ? 0 : form.priceVnd,
                  })
                }
              >
                <option value="PUBLIC">Public</option>
                <option value="RESTRICTED">Restricted</option>
              </select>
            </label>
            <Input
              label="Price (VND)"
              type="number"
              required
              min={0}
              max={form.accessMode === "RESTRICTED" ? 0 : 1000000000}
              value={form.priceVnd}
              onChange={(e) =>
                setForm({ ...form, priceVnd: Number(e.target.value) })
              }
            />
          </div>
          {current && <p>Access mode is fixed by the current update API.</p>}
          {editable && (
            <Button type="submit" disabled={action.busy || uploading}>
              {action.busy ? "Saving…" : "Save resource"}
            </Button>
          )}
        </fieldset>
      </form>
      {current && (
        <MediaPanel
          scope="LIBRARY"
          target={current.id}
          writable={editable}
          removable={editable}
          onBusyChange={setUploading}
        />
      )}
    </>
  );
}
