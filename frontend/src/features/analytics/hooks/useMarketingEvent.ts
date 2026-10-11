import { useEffect, useRef } from "react";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { analyticsApi } from "../api/analyticsApi";
const visitorId = crypto.randomUUID();
export function useMarketingEvent(
  organizationId: string | undefined,
  versionId: string | undefined,
  kind: "IMPRESSION" | "DETAIL_VIEW" | "ORG_VIEW",
) {
  const { user } = useAuth();
  const ref = useRef<HTMLElement | null>(null);
  const sent = useRef(new Set<string>());
  useEffect(() => {
    if (!organizationId) return;
    const key = `${organizationId}:${versionId}:${kind}:${user?.id ?? "guest"}`;
    let stopped = false;
    const send = () => {
      if (
        stopped ||
        document.visibilityState !== "visible" ||
        sent.current.has(key)
      )
        return;
      sent.current.add(key);
      const q = new URLSearchParams(window.location.search);
      void analyticsApi
        .event({
          organizationId,
          versionId: versionId ?? null,
          visitorId,
          requestId: crypto.randomUUID(),
          kind,
          source: (q.get("utm_source") ?? "direct").slice(0, 120),
          campaign: (q.get("utm_campaign") ?? "").slice(0, 120),
        })
        .catch(() => sent.current.delete(key));
    };
    if (kind !== "IMPRESSION") {
      send();
      return () => {
        stopped = true;
      };
    }
    const node = ref.current;
    if (!node) return;
    let timer: ReturnType<typeof setTimeout> | undefined;
    let visible = false;
    const cancel = () => {
      if (timer) clearTimeout(timer);
      timer = undefined;
    };
    const schedule = () => {
      cancel();
      if (visible && document.visibilityState === "visible")
        timer = setTimeout(send, 1000);
    };
    const observer = new IntersectionObserver(
      (entries) => {
        visible = entries.some(
          (e) => e.isIntersecting && e.intersectionRatio >= 0.5,
        );
        schedule();
      },
      { threshold: [0, 0.5] },
    );
    observer.observe(node);
    document.addEventListener("visibilitychange", schedule);
    return () => {
      stopped = true;
      cancel();
      observer.disconnect();
      document.removeEventListener("visibilitychange", schedule);
    };
  }, [organizationId, versionId, kind, user?.id]);
  return ref;
}
