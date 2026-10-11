import { useCallback } from "react";
import { Link } from "react-router-dom";
import { apiClient } from "@/shared/api/apiClient";
import { useRemote } from "@/shared/hooks/useRemote";
import { Bell } from "lucide-react";
export function NotificationButton() {
  const remote = useRemote(
    useCallback(
      () => apiClient.get<{ count: number }>("/workspace/notifications/unread"),
      [],
    ),
  );
  return (
    <Link
      className="sporg-notification-link"
      to="/organizer/activity"
      aria-label={
        remote.data
          ? `${remote.data.count} unread notifications`
          : "Notifications"
      }
    >
      <Bell size={18} />
      {remote.data && remote.data.count > 0 && <span>{remote.data.count}</span>}
    </Link>
  );
}
