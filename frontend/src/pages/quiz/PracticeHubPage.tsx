import { useEffect, useState } from "react"
import { ArrowRight, BookOpenCheck, Swords, Target } from "lucide-react"
import { Link } from "react-router-dom"

import LearnerSurface from "@/components/layout/LearnerSurface"
import Button from "@/components/ui/Button"
import { useAuth } from "@/features/auth/hooks/useAuth"
import { learningApi, type Enrollment } from "@/features/learning/learningApi"

export default function PracticeHubPage() {
  const { user } = useAuth()
  const [items, setItems] = useState<Enrollment[]>([])
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (user?.role !== "LEARNER") return
    setLoading(true)
    learningApi.mine().then((data) => { setItems(data); setError("") }).catch((cause) => setError(cause instanceof Error ? cause.message : "We couldn't load your available practice.")).finally(() => setLoading(false))
  }, [user?.id, user?.role])

  return (
    <LearnerSurface>
      <main className="learner-page learner-page--practice">
        <header className="practice-hub-hero">
          <div><span><Target size={16} /> Practice with a purpose</span><h1>Turn what you know<br />into something <em>stronger.</em></h1><p>Practice gives you feedback. Official assessments prove completion. Realtime challenges test how you respond under pressure.</p></div>
          {user?.role === "LEARNER" ? <Link className="practice-hub-hero__challenge" to="/app/challenge"><Swords size={22} /><span><strong>Try a 1v1 challenge</strong><small>Match by skill and test yourself live.</small></span><ArrowRight size={18} /></Link> : null}
        </header>

        <section className="learner-content-section">
          <div className="learner-content-section__heading"><div><h2>Practice from your learning</h2><p>Open a path to see the quizzes and assessments available for its published version.</p></div></div>
          {user?.role !== "LEARNER" ? <div className="learner-empty"><BookOpenCheck size={24} /><h3>Practice starts with a learning path.</h3><p>Explore published paths, then sign in as a Learner when you are ready to begin.</p><Button asChild><Link to="/learning-paths">Explore learning <ArrowRight size={16} /></Link></Button></div>
            : loading ? <div className="learner-state" role="status">Finding your available practice…</div>
            : error ? <div className="learner-state learner-state--error" role="alert"><strong>Practice is unavailable right now.</strong><span>{error}</span></div>
            : items.length ? <div className="practice-path-list">{items.map((item) => <Link key={item.id} to={`/app/learning/${item.id}`} className="practice-path-row"><span><BookOpenCheck size={20} /></span><div><strong>{item.title}</strong><small>Open this path to practice or take available assessments.</small></div><em>Version {item.version_no}</em><ArrowRight size={17} /></Link>)}</div>
            : <div className="learner-empty"><BookOpenCheck size={24} /><h3>Choose something worth practicing.</h3><p>Once you enroll in a path, its available practice and assessments will show up here.</p><Button asChild><Link to="/learning-paths">Explore learning <ArrowRight size={16} /></Link></Button></div>}
        </section>
      </main>
    </LearnerSurface>
  )
}
