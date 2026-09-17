import { Zap, Shield, BarChart2 } from 'lucide-react';
import Container from '@/components/ui/Container';
import SectionHeading from '@/components/ui/SectionHeading';
import Progress from '@/components/ui/Progress';

function ChallengePreview() {
  return (
    <div
      className="rounded-2xl p-6"
      style={{ backgroundColor: '#1E2028', border: '1px solid #2A2D38' }}
    >
      {/* Header */}
      <div className="flex items-center justify-between mb-5">
        <div className="flex items-center gap-2">
          <span
            className="text-xs font-semibold uppercase tracking-wide px-2 py-0.5 rounded"
            style={{ backgroundColor: '#FF4F8B22', color: '#FF4F8B' }}
          >
            Realtime Challenge
          </span>
          <span className="flex items-center gap-1 text-xs text-green-400">
            <span className="w-1.5 h-1.5 rounded-full bg-green-400 animate-pulse" /> Live
          </span>
        </div>
        <span className="text-xl font-bold tabular-nums" style={{ color: '#FF4F8B' }}>0:23</span>
      </div>

      {/* Players */}
      <div className="grid grid-cols-2 gap-4 mb-5">
        {[
          { name: 'You', correct: 3, progress: 60 },
          { name: 'Opponent', correct: 2, progress: 40 },
        ].map((player) => (
          <div key={player.name}>
            <div className="flex items-center justify-between mb-1.5">
              <span className="text-sm font-medium text-white">{player.name}</span>
              <span className="text-xs text-gray-400">{player.correct} correct</span>
            </div>
            <Progress value={player.progress} size="sm" color="brand" />
          </div>
        ))}
      </div>

      {/* Question */}
      <div
        className="rounded-lg p-4 mb-4"
        style={{ backgroundColor: '#111318' }}
      >
        <p className="text-xs text-gray-400 mb-1">Question 7 of 10</p>
        <p className="text-sm text-white font-medium">Which HTTP method is idempotent?</p>
      </div>

      {/* Answer options */}
      <div className="grid grid-cols-2 gap-2 mb-4">
        {['GET', 'POST', 'PATCH', 'DELETE'].map((opt, i) => (
          <button
            key={opt}
            className="rounded-lg px-3 py-2 text-sm font-medium text-left transition-colors"
            style={
              i === 0
                ? { backgroundColor: '#FF4F8B22', color: '#FF4F8B', border: '1px solid #FF4F8B' }
                : { backgroundColor: '#2A2D38', color: '#9CA3AF', border: '1px solid transparent' }
            }
          >
            <span className="mr-2 opacity-60">{String.fromCharCode(65 + i)}.</span>
            {opt}
          </button>
        ))}
      </div>

      {/* Connection */}
      <p className="text-xs text-gray-500 text-center">
        Connected • Server-authoritative scoring
      </p>
    </div>
  );
}

const features = [
  {
    icon: Shield,
    title: 'Fair competition',
    description: 'Server controls all timing and scoring — no client-side manipulation possible',
  },
  {
    icon: Zap,
    title: 'Instant feedback',
    description: 'Know your result immediately after each question',
  },
  {
    icon: BarChart2,
    title: 'Skill measurement',
    description: 'Performance tracked per topic to identify knowledge gaps',
  },
];

export default function RealtimeSection() {
  return (
    <section className="py-24" style={{ backgroundColor: '#111318' }}>
      <Container>
        <div className="grid md:grid-cols-2 gap-12 lg:gap-20 items-center">
          <div>
            <SectionHeading
              eyebrow="REALTIME CHALLENGES"
              title="Practice under pressure. Learn through competition."
              subtitle="1v1 challenges put your knowledge to the test in real time. Both participants are synchronized through a server-authoritative engine — no shortcuts, no memorized answers."
              dark
            />

            <div className="space-y-6 mt-8">
              {features.map((f) => (
                <div key={f.title} className="flex gap-4">
                  <div
                    className="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5"
                    style={{ backgroundColor: '#FF4F8B22' }}
                  >
                    <f.icon size={16} color="#FF4F8B" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-white text-sm mb-0.5">{f.title}</h3>
                    <p className="text-sm text-gray-400">{f.description}</p>
                  </div>
                </div>
              ))}
            </div>

            <p className="text-xs text-gray-500 mt-8 leading-relaxed">
              Server-authoritative scoring keeps both participants synchronized in real-time.
              Your progress cannot be manipulated.
            </p>
          </div>

          <div>
            <ChallengePreview />
          </div>
        </div>
      </Container>
    </section>
  );
}
