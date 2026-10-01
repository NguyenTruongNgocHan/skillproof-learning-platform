import { Zap, Shield, BarChart2 } from "lucide-react"
import Container from "@/components/ui/Container"
import SectionHeading from "@/components/ui/SectionHeading"
import Progress from "@/components/ui/Progress"

function ChallengePreview() {
  return (
    <div
      className="rounded-2xl p-6"
      style={{
        backgroundColor: "var(--surface)",
        border: "1px solid var(--border)",
      }}
    >
      {/* Header */}
      <div className="flex items-center justify-between mb-5">
        <div className="flex items-center gap-2">
          <span
            className="text-xs font-semibold uppercase tracking-wide px-2 py-0.5 rounded"
            style={{
              backgroundColor: "var(--brand-soft)",
              color: "var(--brand)",
            }}
          >
            Realtime Challenge
          </span>
          <span className="text-xs text-muted-skin">Illustrative preview</span>
        </div>
        <span
          className="text-xl font-bold tabular-nums"
          style={{ color: "var(--brand)" }}
        >
          0:23
        </span>
      </div>

      {/* Players */}
      <div className="grid grid-cols-2 gap-4 mb-5">
        {[
          { name: "You", correct: 3, progress: 60 },
          { name: "Opponent", correct: 2, progress: 40 },
        ].map((player) => (
          <div key={player.name}>
            <div className="flex items-center justify-between mb-1.5">
              <span className="text-sm font-medium text-skin">
                {player.name}
              </span>
              <span className="text-xs text-muted-skin">
                {player.correct} correct
              </span>
            </div>
            <Progress value={player.progress} size="sm" color="brand" />
          </div>
        ))}
      </div>

      {/* Question */}
      <div
        className="rounded-lg p-4 mb-4"
        style={{ backgroundColor: "var(--bg-subtle)" }}
      >
        <p className="text-xs text-muted-skin mb-1">Question 7 of 10</p>
        <p className="text-sm text-skin font-medium">
          Which HTTP method retrieves a resource without changing it?
        </p>
      </div>

      {/* Answer options */}
      <div className="grid grid-cols-2 gap-2 mb-4">
        {["GET", "POST", "PATCH", "DELETE"].map((opt, i) => (
          <div
            key={opt}
            className="rounded-lg px-3 py-2 text-sm font-medium text-left transition-colors"
            style={
              i === 0
                ? {
                    backgroundColor: "var(--brand-soft)",
                    color: "var(--brand)",
                    border: "1px solid var(--brand)",
                  }
                : {
                    backgroundColor: "var(--border)",
                    color: "var(--fg-muted)",
                    border: "1px solid transparent",
                  }
            }
          >
            <span className="mr-2 opacity-60">
              {String.fromCharCode(65 + i)}.
            </span>
            {opt}
          </div>
        ))}
      </div>

      {/* Connection */}
      <p className="text-xs text-muted-skin text-center">
        Concept preview of server-controlled scoring
      </p>
    </div>
  )
}

const features = [
  {
    icon: Shield,
    title: "Server-controlled scoring",
    description: "The planned server will control timing and scoring",
  },
  {
    icon: Zap,
    title: "Instant feedback",
    description: "Know your result immediately after each question",
  },
  {
    icon: BarChart2,
    title: "Skill measurement",
    description: "Planned topic-level feedback to help identify knowledge gaps",
  },
]

export default function RealtimeSection() {
  return (
    <section className="py-24 bg-skin">
      <Container>
        <div className="grid md:grid-cols-2 gap-12 lg:gap-20 items-center">
          <div>
            <SectionHeading
              eyebrow="REALTIME CHALLENGES"
              title="Practice under pressure. Learn through competition."
              subtitle="The planned 1v1 experience will synchronize questions, answers and scores through the server. It is not live yet."
            />

            <div className="space-y-6 mt-8">
              {features.map((f) => (
                <div key={f.title} className="flex gap-4">
                  <div
                    className="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5"
                    style={{ backgroundColor: "var(--brand-soft)" }}
                  >
                    <f.icon size={16} color="var(--brand)" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-skin text-sm mb-0.5">
                      {f.title}
                    </h3>
                    <p className="text-sm text-muted-skin">{f.description}</p>
                  </div>
                </div>
              ))}
            </div>

            <p className="text-xs text-muted-skin mt-8 leading-relaxed">
              Concept preview. Challenge results are not recorded in this
              release.
            </p>
          </div>

          <div>
            <ChallengePreview />
          </div>
        </div>
      </Container>
    </section>
  )
}
