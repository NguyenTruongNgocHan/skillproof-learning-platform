import { Link } from "react-router-dom";
import Container from "@/components/ui/Container";
import Button from "@/components/ui/Button";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { getNextRouteForUserState } from "@/utils/authFlow";

export default function FinalCTASection() {
  const { user } = useAuth();
  const accountTarget = user ? getNextRouteForUserState(user) : "/register";
  return (
    <section className="py-24" style={{ backgroundColor: "var(--bg-subtle)" }}>
      <Container>
        <div className="text-center">
          <h2 className="text-4xl md:text-5xl font-bold text-skin mb-5">
            Turn learning into proof.
          </h2>
          <p className="text-lg text-muted-skin max-w-xl mx-auto mb-10">
            Start with your account today. Learning Paths, assessments and verifiable certificates are planned for future releases.
          </p>

          <div className="flex flex-wrap justify-center gap-4 mb-6">
            <Button asChild variant="primary" size="lg">
              <Link to={accountTarget}>{user ? "My workspace" : "Create an account"}</Link>
            </Button>
            <Link
              to="/verify"
              className="px-6 py-3 text-base font-medium rounded-xl border border-skin text-skin bg-transparent hover:bg-brand-soft-skin transition-colors"
            >
              Verification roadmap
            </Link>
          </div>

          <a href="#organizations" className="text-sm text-muted-skin hover:text-brand-skin transition-colors">
            For organizations →
          </a>
        </div>
      </Container>
    </section>
  );
}
