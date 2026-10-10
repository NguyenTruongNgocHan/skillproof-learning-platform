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
  useEffect(() => {
    setDirty(`workspace-dialog-${key}`, true);
    return () => setDirty(`workspace-dialog-${key}`, false);
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
        if (!busy) close();
      }}
    >
      <header>
        <h2>{title}</h2>
        <button
          type="button"
          disabled={busy}
          onClick={close}
          aria-label="Close dialog"
        >
          <X size={18} />
        </button>
      </header>
      <div className="sporg skillproof-app-theme">{children}</div>
    </dialog>
  );
}
