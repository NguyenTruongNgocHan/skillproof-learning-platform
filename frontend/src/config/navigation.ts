import { LayoutDashboard, BookOpen, Target, Zap, TrendingUp, Award, Users, HelpCircle, ClipboardList, FileCheck, BadgeCheck, BarChart2, Settings, CheckSquare, Building2, MessageSquare, FileText } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import type { UserRole } from '@/features/auth/types/auth.types';

export interface NavItem {
  label: string;
  path: string;
  icon: LucideIcon;
  badge?: number;
}

export const LEARNER_NAV: NavItem[] = [
  { label: 'Overview', path: '/app', icon: LayoutDashboard },
  { label: 'Learning Paths', path: '/app/learning-paths', icon: BookOpen },
  { label: 'Practice', path: '/app/practice', icon: Target },
  { label: 'Challenges', path: '/app/challenges', icon: Zap },
  { label: 'Progress', path: '/app/progress', icon: TrendingUp },
  { label: 'Certificates', path: '/app/certificates', icon: Award },
  { label: 'Community', path: '/community', icon: Users },
];

export const ORGANIZER_NAV: NavItem[] = [
  { label: 'Overview', path: '/organizer', icon: LayoutDashboard },
  { label: 'Learning Paths', path: '/organizer/learning-paths', icon: BookOpen },
  { label: 'Question Bank', path: '/organizer/question-bank', icon: HelpCircle },
  { label: 'Quizzes', path: '/organizer/quizzes', icon: ClipboardList },
  { label: 'Assessments', path: '/organizer/assessments', icon: FileCheck },
  { label: 'Certifications', path: '/organizer/certifications', icon: Award },
  { label: 'Certificates', path: '/organizer/certificates', icon: BadgeCheck },
  { label: 'Learners', path: '/organizer/learners', icon: Users },
  { label: 'Analytics', path: '/organizer/analytics', icon: BarChart2 },
  { label: 'Settings', path: '/organizer/settings', icon: Settings },
];

export const ADMIN_NAV: NavItem[] = [
  { label: 'Overview', path: '/admin', icon: LayoutDashboard },
  { label: 'Users', path: '/admin/users', icon: Users },
  { label: 'Organizations', path: '/admin/organizations', icon: Building2 },
  { label: 'Approvals', path: '/admin/approvals', icon: CheckSquare, badge: 3 },
  { label: 'Learning Paths', path: '/admin/learning-paths', icon: BookOpen },
  { label: 'Moderation', path: '/admin/moderation', icon: MessageSquare },
  { label: 'Certificates', path: '/admin/certificates', icon: Award },
  { label: 'Audit Logs', path: '/admin/audit', icon: FileText },
  { label: 'System', path: '/admin/system', icon: Settings },
];

export function getNavForRole(role: UserRole): NavItem[] {
  switch (role) {
    case 'LEARNER': return LEARNER_NAV;
    case 'ORGANIZER': return ORGANIZER_NAV;
    case 'ADMIN': return ADMIN_NAV;
  }
}
