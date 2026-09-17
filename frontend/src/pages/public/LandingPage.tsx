import PublicHeader from '@/components/layout/PublicHeader';
import PublicFooter from '@/components/layout/PublicFooter';
import HeroSection from '@/features/marketing/components/HeroSection';
import TrustSection from '@/features/marketing/components/TrustSection';
import JourneySection from '@/features/marketing/components/JourneySection';
import LearningPathsSection from '@/features/marketing/components/LearningPathsSection';
import RealtimeSection from '@/features/marketing/components/RealtimeSection';
import CertificationSection from '@/features/marketing/components/CertificationSection';
import VerificationSection from '@/features/marketing/components/VerificationSection';
import RecommendationSection from '@/features/marketing/components/RecommendationSection';
import CommunitySection from '@/features/marketing/components/CommunitySection';
import OrganizerSection from '@/features/marketing/components/OrganizerSection';
import FinalCTASection from '@/features/marketing/components/FinalCTASection';

export default function LandingPage() {
  return (
    <div className="min-h-full flex flex-col">
      <PublicHeader />
      <main className="flex-1">
        <HeroSection />
        <TrustSection />
        <JourneySection />
        <LearningPathsSection />
        <RealtimeSection />
        <CertificationSection />
        <VerificationSection />
        <RecommendationSection />
        <CommunitySection />
        <OrganizerSection />
        <FinalCTASection />
      </main>
      <PublicFooter />
    </div>
  );
}
