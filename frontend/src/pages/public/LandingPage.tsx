import { useEffect } from "react";
import PublicHeader from "@/components/layout/PublicHeader";
import PublicFooter from "@/components/layout/PublicFooter";
import HeroSection from "@/features/marketing/components/HeroSection";
import TrustSection from "@/features/marketing/components/TrustSection";
import JourneySection from "@/features/marketing/components/JourneySection";
import LearningPathsSection from "@/features/marketing/components/LearningPathsSection";
import RealtimeSection from "@/features/marketing/components/RealtimeSection";
import CertificationSection from "@/features/marketing/components/CertificationSection";
import VerificationSection from "@/features/marketing/components/VerificationSection";
import RecommendationSection from "@/features/marketing/components/RecommendationSection";
import CommunitySection from "@/features/marketing/components/CommunitySection";
import OrganizerSection from "@/features/marketing/components/OrganizerSection";
import FinalCTASection from "@/features/marketing/components/FinalCTASection";

export default function LandingPage() {
  useEffect(() => {
    if (
      window.matchMedia("(prefers-reduced-motion: reduce)").matches ||
      !("IntersectionObserver" in window)
    )
      return;
    const root = document.querySelector(".landing-page");
    const nodes = root?.querySelectorAll("[data-reveal]");
    if (!root || !nodes) return;
    const observer = new IntersectionObserver(
      (entries) =>
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("is-visible");
            observer.unobserve(entry.target);
          }
        }),
      { threshold: 0.1, rootMargin: "0px 0px -30px 0px" },
    );
    nodes.forEach((node) => observer.observe(node));
    root.classList.add("motion-ready");
    return () => {
      observer.disconnect();
      root.classList.remove("motion-ready");
    };
  }, []);
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
          <RealtimeSection />
        </div>
        <div id="credentials" data-reveal>
          <CertificationSection />
        </div>
        <div data-reveal>
          <VerificationSection />
        </div>
        <div data-reveal>
          <RecommendationSection />
        </div>
        <div data-reveal>
          <CommunitySection />
        </div>
        <div id="organizations" data-reveal>
          <OrganizerSection />
        </div>
        <FinalCTASection />
      </main>
      <PublicFooter />
    </div>
  );
}
