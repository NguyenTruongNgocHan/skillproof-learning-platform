import type { ReactNode } from 'react';
import { ArrowUpRight, BookOpen, CheckCircle2, Clock3, ShieldCheck, Sparkles, Users, Zap } from 'lucide-react';
import { useAuth } from '@/features/auth/hooks/useAuth';
import Button from '@/components/ui/Button';

type RoleKind = 'learner' | 'organizer' | 'admin';
const roleContent = {
  learner: { eyebrow: 'Learner workspace', title: 'Keep your momentum going', description: 'Your learning, assessments, and verified achievements — in one focused workspace.', metrics: [['Weekly goal', '3 / 5 days'], ['Path progress', '68%'], ['Certificates', '2']], primary: 'Continue learning', secondary: 'View learning plan' },
  organizer: { eyebrow: 'Organization workspace', title: 'Build learning people can trust', description: 'Publish structured programs, evaluate skills, and issue credentials with clear evidence.', metrics: [['Active learners', '1,284'], ['Completion rate', '82%'], ['Credentials issued', '438']], primary: 'Create learning path', secondary: 'Review submissions' },
  admin: { eyebrow: 'Platform operations', title: 'Trust and safety at a glance', description: 'Review identity activity, organization verification, and platform health from one place.', metrics: [['Active accounts', '12,842'], ['Pending reviews', '18'], ['System health', 'Healthy']], primary: 'Review approvals', secondary: 'Open audit log' },
} as const;

function Panel({ children, className = '' }: { children: ReactNode; className?: string }) { return <section className={`dashboard-panel ${className}`}>{children}</section>; }

export default function RoleDashboard({ kind }: { kind: RoleKind }) {
  const { user } = useAuth();
  const content = roleContent[kind];
  const firstName = user?.fullName?.trim().split(/\s+/).at(-1) || 'there';
  const isLearner = kind === 'learner';
  return <div className={`role-dashboard role-dashboard--${kind}`}>
    <section className="dashboard-hero">
      <div><p className="dashboard-eyebrow"><Sparkles size={14} /> {content.eyebrow}</p><h1>{isLearner ? `Welcome back, ${firstName}` : content.title}</h1><p>{content.description}</p><div className="dashboard-actions"><Button variant="primary">{content.primary} <ArrowUpRight size={16} /></Button><Button variant="outline">{content.secondary}</Button></div></div>
      <div className="dashboard-hero-orbit" aria-hidden="true"><div className="orbit-core"><ShieldCheck size={30} /></div><span className="orbit-dot orbit-dot--one" /><span className="orbit-dot orbit-dot--two" /></div>
    </section>
    <div className="metric-grid">{content.metrics.map(([label, value], index) => <Panel key={label} className="metric-card"><span>{label}</span><strong>{value}</strong><small>{index === 1 ? '+8% this month' : 'Updated moments ago'}</small></Panel>)}</div>
    <div className="dashboard-grid">
      <Panel className="dashboard-focus"><div className="panel-heading"><div><span>Priority</span><h2>{isLearner ? 'Backend Engineering Path' : kind === 'organizer' ? 'Programs needing attention' : 'Identity review queue'}</h2></div><Zap size={20} /></div><div className="focus-progress"><span style={{ width: isLearner ? '68%' : '76%' }} /></div><div className="activity-list"><div><CheckCircle2 /><span><b>{isLearner ? 'API Design & REST' : 'Account verification'}</b><small>Completed and securely recorded</small></span><time>Done</time></div><div><Clock3 /><span><b>{isLearner ? 'Cloud Fundamentals' : 'Organization review'}</b><small>Ready for your next action</small></span><time>Today</time></div><div><BookOpen /><span><b>{isLearner ? 'Final assessment' : 'Evidence and audit trail'}</b><small>Unlock the next milestone</small></span><time>Next</time></div></div></Panel>
      <Panel className="dashboard-side-card"><div className="panel-heading"><div><span>Live snapshot</span><h2>{kind === 'admin' ? 'Platform trust' : 'Your network'}</h2></div><Users size={20} /></div><div className="snapshot-ring"><strong>{kind === 'admin' ? '99.9%' : '24'}</strong><span>{kind === 'admin' ? 'uptime' : 'new this week'}</span></div><p>Identity foundation is connected and ready for the next product phases.</p></Panel>
    </div>
  </div>;
}
