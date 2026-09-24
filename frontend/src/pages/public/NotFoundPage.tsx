import { Link } from "react-router-dom";
import PublicHeader from "@/components/layout/PublicHeader";
import PublicFooter from "@/components/layout/PublicFooter";
import Button from "@/components/ui/Button";
export default function NotFoundPage() {
  return (
    <div className="public-page">
      <PublicHeader />
      <main className="public-message-page">
        <div className="public-message-card">
          <span className="eyebrow">404 · NO ROUTE HERE</span>
          <h1>We couldn't find this page.</h1>
          <p>
            The address may have changed. You can return to the homepage or use
            the navigation above.
          </p>
          <Button asChild variant="primary">
            <Link to="/">Return to home</Link>
          </Button>
        </div>
      </main>
      <PublicFooter />
    </div>
  );
}
