import CertificationSection from "@/features/marketing/components/CertificationSection"
import CommunitySection from "@/features/marketing/components/CommunitySection"
import FinalCTASection from "@/features/marketing/components/FinalCTASection"
import HeroSection from "@/features/marketing/components/HeroSection"
import JourneySection from "@/features/marketing/components/JourneySection"
import LearningPathsSection from "@/features/marketing/components/LearningPathsSection"
import OrganizerSection from "@/features/marketing/components/OrganizerSection"
import RealtimeSection from "@/features/marketing/components/RealtimeSection"
import RecommendationSection from "@/features/marketing/components/RecommendationSection"
import TrustSection from "@/features/marketing/components/TrustSection"
import VerificationSection from "@/features/marketing/components/VerificationSection"
import PublicFooter from "@/shared/components/layout/PublicFooter"
import PublicHeader from "@/shared/components/layout/PublicHeader"
import { useEffect } from "react"

export default function LandingPage() {
  useEffect(() => {
    if (
      window.matchMedia("(prefers-reduced-motion: reduce)").matches ||
      !("IntersectionObserver" in window)
    )
      return

    const root = document.querySelector(".landing-page")
    const nodes = root?.querySelectorAll("[data-reveal]")
    if (!root || !nodes) return
    const observer = new IntersectionObserver(
      (entries) =>
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("is-visible")
            observer.unobserve(entry.target)
          }
        }),
      { threshold: 0.1, rootMargin: "0px 0px -30px 0px" },
    )
    nodes.forEach((node) => observer.observe(node))
    root.classList.add("motion-ready")
    return () => {
      observer.disconnect()
      root.classList.remove("motion-ready")
    }
  }, [])

  return (
    <div className="landing-page min-h-full flex flex-col">
      <PublicHeader />
      <main className="flex-1">
        <HeroSection />
        <TrustSection />
        <div id="how-it-works" data-reveal>
          <JourneySection />
        </div>
        <div id="learning" data-reveal>
          <LearningPathsSection />
        </div>
        <div data-reveal>
          <RecommendationSection />
        </div>
        <div data-reveal>
          <RealtimeSection />
        </div>
        <div data-reveal>
          <CommunitySection />
        </div>
        <div id="organizations" data-reveal>
          <OrganizerSection />
        </div>
        <div id="credentials" data-reveal>
          <CertificationSection />
        </div>
        <div data-reveal>
          <VerificationSection />
        </div>
        <FinalCTASection />
      </main>
      <PublicFooter />
    </div>
  )
}
