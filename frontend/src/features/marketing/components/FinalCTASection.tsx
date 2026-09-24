import { Link } from "react-router-dom";
import Container from "@/components/ui/Container";
import Button from "@/components/ui/Button";

export default function FinalCTASection() {
  return (
    <section className="py-24" style={{ backgroundColor: "#111318" }}>
      <Container>
        <div className="text-center">
          <h2 className="text-4xl md:text-5xl font-bold text-white mb-5">
            Turn learning into proof.
          </h2>
          <p className="text-lg text-gray-400 max-w-xl mx-auto mb-10">
            Build practical skills through structured learning and leave with
            credentials designed to be used beyond the platform.
          </p>

          <div className="flex flex-wrap justify-center gap-4 mb-6">
            <Button asChild variant="primary" size="lg">
              <Link to="/register">Create an account</Link>
            </Button>
            <Link to="/verify">
              <button className="px-6 py-3 text-base font-medium rounded-xl border border-white/30 text-white bg-transparent hover:bg-skin/10 transition-colors">
                Verify a Certificate
              </button>
            </Link>
          </div>

          <Link
            to="/register"
            className="text-sm text-gray-500 hover:text-gray-300 transition-colors"
          >
            For Organizations →
          </Link>
        </div>
      </Container>
    </section>
  );
}
