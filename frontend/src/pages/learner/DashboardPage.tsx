import AppShell from "@/components/layout/AppShell";
import RoleDashboard from '@/features/dashboard/RoleDashboard';

export default function DashboardPage() {
  return <AppShell><RoleDashboard kind="learner" /></AppShell>;
}
