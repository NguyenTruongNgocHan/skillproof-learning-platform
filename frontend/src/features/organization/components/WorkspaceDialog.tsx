import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { useId } from "react";
import { useEffect, useRef, type ReactNode } from "react";
import { X } from "lucide-react";
export default function WorkspaceDialog({
  title,
  children,
  close,
  busy = false,
}: {
  title: string;
  children: ReactNode;
  close: () => void;
  busy?: boolean;
}) {
  const dialog = useRef<HTMLDialogElement>(null);
  const { setDirty } = useOrganizationContext();
  const key = useId();
  const edited = useRef(false);
  const requestClose = () => {
    if (busy) return;
    if (edited.current && !window.confirm("Discard unsaved dialog changes?"))
      return;
    close();
  };
  useEffect(() => {
    const node = dialog.current;
    const changed = () => {
      edited.current = true;
      setDirty(`workspace-dialog-${key}`, true);
    };
    const saved = () => {
      edited.current = false;
      setDirty(`workspace-dialog-${key}`, false);
    };
    node?.addEventListener("input", changed);
    node?.addEventListener("change", changed);
    node?.addEventListener("workspace:saved", saved);
    return () => {
      node?.removeEventListener("input", changed);
      node?.removeEventListener("change", changed);
      node?.removeEventListener("workspace:saved", saved);
      setDirty(`workspace-dialog-${key}`, false);
    };
  }, [key, setDirty]);
  useEffect(() => {
    const node = dialog.current;
    node?.showModal();
    return () => node?.close();
  }, []);
  return (
    <dialog
      ref={dialog}
      className="workspace-dialog"
      aria-label={title}
      onCancel={(e) => {
        e.preventDefault();
        requestClose();
      }}
    >
      <header>
        <h2>{title}</h2>
        <button
          type="button"
          disabled={busy}
          onClick={requestClose}
          aria-label="Close dialog"
        >
          <X size={18} />
        </button>
      </header>
      <div className="sporg skillproof-app-theme">{children}</div>
    </dialog>
  );
}
