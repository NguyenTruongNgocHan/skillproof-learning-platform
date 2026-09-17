import { Link } from 'react-router-dom';
import Container from '@/components/ui/Container';
import Button from '@/components/ui/Button';

export default function NotFoundPage() {
  return (
    <div className="min-h-screen bg-white flex items-center justify-center">
      <Container className="max-w-lg text-center">
        <p className="text-7xl font-bold mb-4" style={{ color: '#FF4F8B' }}>404</p>
        <h1 className="text-2xl font-bold text-[#17181C] mb-3">Page not found</h1>
        <p className="text-[#666A73] mb-8">
          The page you are looking for does not exist or has been moved.
        </p>
        <Link to="/">
          <Button variant="primary" size="md">Return to homepage</Button>
        </Link>
      </Container>
    </div>
  );
}
