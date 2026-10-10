import PublicFooter from "@/shared/components/layout/PublicFooter"
import PublicHeader from "@/shared/components/layout/PublicHeader"
import { ArrowLeft, Clock3 } from "lucide-react"
import { Link } from "react-router-dom"
interface ComingSoonPageProps {
  title: string
  description?: string
}
export default function ComingSoonPage({ title, description }: ComingSoonPageProps) {
  return (
    <div className="public-page">
      <PublicHeader />
      <main className="public-message-page">
        <div className="public-message-card">
          <div className="public-message-icon">
            <Clock3 size={27} />
          </div>
          <span className="eyebrow">ON THE ROADMAP</span>
          <h1>{title}</h1>
          <p>{description}</p>
          <p>
            This feature has not launched. Your current account and Organization flows are available
            now.
          </p>
          <Link to="/" className="profile-back">
            <ArrowLeft size={16} /> Back to home
          </Link>
        </div>
      </main>
      <PublicFooter />
    </div>
  )
}
