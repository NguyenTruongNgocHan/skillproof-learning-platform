import { CheckCircle, ShieldCheck } from 'lucide-react';
import { Link } from 'react-router-dom';
import Button from '@/components/ui/Button';
import Container from '@/components/ui/Container';
import Progress from '@/components/ui/Progress';

function ProductCard() {
  return (
    <div
      className="rounded-2xl p-6 relative overflow-hidden"
      style={{
        backgroundColor: '#181A20',
        borderLeft: '4px solid #FF4F8B',
        boxShadow: '0 24px 64px rgba(0,0,0,0.35)',
      }}
    >
      {/* Card header */}
      <div className="mb-5">
        <h3 className="text-white font-semibold text-lg">Backend Engineering Path</h3>
        <p className="text-gray-400 text-sm mt-0.5">SkillProof Academy • Intermediate</p>
      </div>

      {/* Progress */}
      <div className="mb-5">
        <div className="flex justify-between items-center mb-2">
          <span className="text-sm font-medium text-white">68% Complete</span>
          <span className="text-xs text-gray-400">26 of 38 lessons</span>
        </div>
        <Progress value={68} size="md" color="brand" />
      </div>

      {/* Module list */}
      <div className="space-y-2.5 mb-5">
        {[
          { label: 'Programming Foundations', done: true },
          { label: 'API Design & REST Principles', done: true },
          { label: 'Database Systems & SQL', done: true },
          { label: 'Cloud Fundamentals', done: false },
        ].map((m) => (
          <div key={m.label} className="flex items-center gap-2.5">
            {m.done ? (
              <CheckCircle size={16} color="#22c55e" className="flex-shrink-0" />
            ) : (
              <div
                className="flex-shrink-0 rounded-full border-2 flex items-center justify-center"
                style={{ width: 16, height: 16, borderColor: '#FF4F8B' }}
              />
            )}
            <span
              className={`text-sm ${m.done ? 'text-gray-300' : 'font-medium'}`}
              style={!m.done ? { color: '#FF4F8B' } : undefined}
            >
              {m.label}
            </span>
          </div>
        ))}
      </div>

      {/* Divider */}
      <div className="border-t border-gray-700 my-4" />

      {/* Certificate eligibility */}
      <div className="mb-5">
        <p className="text-xs font-semibold uppercase tracking-wide text-gray-400 mb-2">
          Certificate Eligibility
        </p>
        <p className="text-sm text-gray-300 mb-2">3 of 4 requirements completed</p>
        <Progress value={75} size="sm" color="brand" />
        <p className="text-xs text-gray-500 mt-2">Assessment required to unlock certificate</p>
      </div>

      {/* Credential badge */}
      <div
        className="flex items-center gap-2 rounded-lg px-3 py-2"
        style={{ backgroundColor: '#FFF0F5' }}
      >
        <ShieldCheck size={16} color="#FF4F8B" />
        <span className="text-xs font-semibold uppercase tracking-wide" style={{ color: '#FF4F8B' }}>
          SkillProof Credential
        </span>
      </div>
    </div>
  );
}

export default function HeroSection() {
  return (
    <section className="py-20 md:py-28 bg-white">
      <Container>
        <div className="grid md:grid-cols-2 gap-12 lg:gap-20 items-center">
          {/* Left */}
          <div>
            <p
              className="text-xs font-semibold uppercase tracking-widest mb-4"
              style={{ color: '#FF4F8B' }}
            >
              Career-Ready Learning &amp; Verifiable Credentials
            </p>
            <h1 className="text-4xl md:text-5xl font-bold text-[#17181C] leading-tight mb-6">
              Build skills.
              <br />
              Prove them.
              <br />
              Take them anywhere.
            </h1>
            <p className="text-lg text-[#666A73] max-w-md mb-8 leading-relaxed">
              Follow structured learning paths, practice through real assessments and realtime challenges,
              then earn credentials employers and organizations can independently verify.
            </p>

            <div className="flex flex-wrap gap-3 mb-8">
              <Link to="/learning-paths">
                <Button variant="primary" size="lg">Explore Learning Paths</Button>
              </Link>
              <Link to="/verify">
                <Button variant="outline" size="lg">Verify a Certificate</Button>
              </Link>
            </div>

            {/* Trust row */}
            <div className="flex flex-wrap gap-5">
              {[
                'Trusted credential infrastructure',
                'Organization-issued certificates',
                'Public verification',
              ].map((item) => (
                <div key={item} className="flex items-center gap-1.5 text-sm text-[#666A73]">
                  <CheckCircle size={14} color="#FF4F8B" />
                  <span>{item}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Right */}
          <div>
            <ProductCard />
          </div>
        </div>
      </Container>
    </section>
  );
}
