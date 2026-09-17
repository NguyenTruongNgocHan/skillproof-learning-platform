import {
  BookOpen,
  Database,
  ClipboardList,
  Settings,
  Award,
  ShieldCheck,
  ShieldOff,
  BarChart2,
  CheckCircle,
} from 'lucide-react';
import { Link } from 'react-router-dom';
import Container from '@/components/ui/Container';
import SectionHeading from '@/components/ui/SectionHeading';
import Button from '@/components/ui/Button';

const capabilities = [
  { icon: BookOpen, title: 'Build structured Learning Paths', description: 'Define modules, lessons, and prerequisite sequences' },
  { icon: Database, title: 'Manage Question Banks', description: 'Author, tag, and organize questions by topic and difficulty' },
  { icon: ClipboardList, title: 'Create Quizzes & Assessments', description: 'Build timed, proctored assessments from your question bank' },
  { icon: Settings, title: 'Define Completion Policies', description: 'Set pass marks, attempt limits, and certification requirements' },
  { icon: Award, title: 'Manage Certification Programs', description: 'Configure multi-requirement certification pathways' },
  { icon: ShieldCheck, title: 'Issue Verifiable Credentials', description: 'Credentials issued via SkillProof\'s verification infrastructure' },
  { icon: ShieldOff, title: 'Revoke Credentials', description: 'Revoke certificates when eligibility conditions are no longer met' },
  { icon: BarChart2, title: 'Track Learner Progress', description: 'Analytics dashboard for enrollment, completion, and certificate rates' },
];

const fakePaths = [
  { name: 'Backend Engineering Foundations', status: 'Published', enrolled: 2841, completion: '72%' },
  { name: 'Software Testing Professional', status: 'Published', enrolled: 1203, completion: '61%' },
  { name: 'Cloud Infrastructure Fundamentals', status: 'Draft', enrolled: 0, completion: '—' },
];

function DashboardPreview() {
  return (
    <div
      className="rounded-2xl overflow-hidden"
      style={{ backgroundColor: '#1E2028', border: '1px solid #2A2D38' }}
    >
      {/* Sidebar + main */}
      <div className="flex h-64">
        {/* Sidebar */}
        <div className="w-40 border-r flex flex-col gap-1 p-3" style={{ borderColor: '#2A2D38', backgroundColor: '#181A20' }}>
          {['Learning Paths', 'Question Bank', 'Certificates', 'Analytics'].map((item, i) => (
            <div
              key={item}
              className="px-3 py-2 rounded-lg text-xs font-medium"
              style={
                i === 0
                  ? { backgroundColor: '#FF4F8B22', color: '#FF4F8B' }
                  : { color: '#9CA3AF' }
              }
            >
              {item}
            </div>
          ))}
        </div>

        {/* Main content */}
        <div className="flex-1 p-4 overflow-hidden">
          <p className="text-xs font-semibold text-white mb-3">Learning Paths</p>
          <div className="space-y-2">
            {fakePaths.map((path) => (
              <div
                key={path.name}
                className="flex items-center gap-2 rounded-lg px-3 py-2"
                style={{ backgroundColor: '#111318' }}
              >
                <div className="flex-1 min-w-0">
                  <p className="text-xs font-medium text-white truncate">{path.name}</p>
                  <p className="text-xs text-gray-500">{path.enrolled > 0 ? `${path.enrolled} enrolled` : 'Not published'}</p>
                </div>
                <span
                  className="text-xs font-medium px-2 py-0.5 rounded flex-shrink-0"
                  style={
                    path.status === 'Published'
                      ? { backgroundColor: '#16a34a22', color: '#22c55e' }
                      : { backgroundColor: '#374151', color: '#9CA3AF' }
                  }
                >
                  {path.status}
                </span>
                <span className="text-xs text-gray-400 flex-shrink-0 w-8 text-right">{path.completion}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function OrganizerSection() {
  return (
    <section className="py-24" style={{ backgroundColor: '#111318' }}>
      <Container>
        <div className="grid md:grid-cols-2 gap-12 lg:gap-20">
          <div>
            <SectionHeading
              eyebrow="FOR ORGANIZATIONS"
              title="Infrastructure for organizations that teach and certify."
              subtitle="The SkillProof organizer workspace gives training providers a complete platform to build, deliver, assess, and certify — without building infrastructure from scratch."
              dark
            />

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {capabilities.map((cap) => (
                <div key={cap.title} className="flex gap-3">
                  <div
                    className="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5"
                    style={{ backgroundColor: '#FF4F8B22' }}
                  >
                    <cap.icon size={15} color="#FF4F8B" />
                  </div>
                  <div>
                    <h3 className="text-sm font-semibold text-white mb-0.5">{cap.title}</h3>
                    <p className="text-xs text-gray-400">{cap.description}</p>
                  </div>
                </div>
              ))}
            </div>

            <div className="mt-8">
              <Link to="/organizer">
                <Button variant="primary" size="md">Request Organizer Access</Button>
              </Link>
            </div>
          </div>

          <div className="flex flex-col justify-center">
            <p className="text-xs font-semibold uppercase tracking-wide text-gray-400 mb-4">
              Organizer Dashboard
            </p>
            <DashboardPreview />

            <div className="mt-5 space-y-2">
              {[
                'Role-based access for instructors and admins',
                'Audit log for all certificate operations',
                'Webhook support for LMS integrations',
              ].map((item) => (
                <div key={item} className="flex items-center gap-2">
                  <CheckCircle size={13} color="#FF4F8B" />
                  <span className="text-xs text-gray-400">{item}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </Container>
    </section>
  );
}
