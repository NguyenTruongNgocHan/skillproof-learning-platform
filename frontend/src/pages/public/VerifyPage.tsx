import PublicHeader from '@/components/layout/PublicHeader';
import PublicFooter from '@/components/layout/PublicFooter';
import VerificationSection from '@/features/marketing/components/VerificationSection';

export default function VerifyPage() {
  return (
    <div className="min-h-full flex flex-col">
      <PublicHeader />
      <main className="flex-1">
        <VerificationSection />
      </main>
      <PublicFooter />
    </div>
  );
}
