import { useCallback, useEffect, useRef, useState } from "react";
import { ApiError } from "@/shared/api/apiClient";
export function requestMessage(error: unknown): string {
  if (error instanceof ApiError && error.status === 404)
    return "This content is unavailable. Refresh or check that the backend supports this page.";
  return error instanceof Error
    ? error.message
    : "Unable to complete the request. Please try again.";
}
export function useRemote<T>(loader: () => Promise<T>) {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [revision, setRevision] = useState(0);
  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    setData(null);
    void loader()
      .then((value) => {
        if (active) setData(value);
      })
      .catch((e) => {
        if (active) setError(requestMessage(e));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [loader, revision]);
  const reload = useCallback(() => setRevision((x) => x + 1), []);
  return { data, error, loading, reload };
}
export function useAction(onDone?: () => void) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const locked = useRef(false);
  const live = useRef(true);
  useEffect(() => {
    live.current = true;
    return () => {
      live.current = false;
    };
  }, []);
  async function run(
    action: () => Promise<unknown>,
    message = "Changes saved.",
  ) {
    if (locked.current) return false;
    locked.current = true;
    setBusy(true);
    setError("");
    setSuccess("");
    try {
      await action();
      if (live.current) {
        setSuccess(message);
        onDone?.();
      }
      return true;
    } catch (e) {
      if (live.current) setError(requestMessage(e));
      return false;
    } finally {
      locked.current = false;
      if (live.current) setBusy(false);
    }
  }
  return { busy, error, success, run };
}
