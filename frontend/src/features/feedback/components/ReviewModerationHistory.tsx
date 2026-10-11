import { useState, useCallback } from "react";
import { feedbackApi } from "../api/feedbackApi";
import { useRemote } from "@/shared/hooks/useRemote";
import { RemoteState } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
export function ReviewModerationHistory({ id }: { id: string }) {
  const [open, setOpen] = useState(false);
  const remote = useRemote(
    useCallback(
      () => (open ? feedbackApi.history(id) : Promise.resolve(null)),
      [id, open],
    ),
  );
  return (
    <section>
      <Button variant="ghost" onClick={() => setOpen(!open)}>
        {open ? "Hide" : "View"} moderation history
      </Button>
      {open && (
        <>
          <RemoteState {...remote} retry={remote.reload} />
          {remote.data?.length === 0 && (
            <p>No moderation decisions recorded.</p>
          )}
          {remote.data?.map((r) => (
            <p key={r.id}>
              {r.decision} · {r.reason}
              <small>
                {new Date(r.createdAt).toLocaleString()} · Moderator {r.actorId}
              </small>
            </p>
          ))}
        </>
      )}
    </section>
  );
}
