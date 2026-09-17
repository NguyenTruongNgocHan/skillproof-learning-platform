import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Menu, X } from 'lucide-react';
import { primaryLight } from '@/assets/brand';
import Button from '@/components/ui/Button';

const navLinks = [
  { label: 'Explore', to: '/explore' },
  { label: 'Learning Paths', to: '/learning-paths' },
  { label: 'Practice', to: '/practice' },
  { label: 'Certifications', to: '/certifications' },
  { label: 'Community', to: '/community' },
];

export default function PublicHeader() {
  const [scrolled, setScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 4);
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  return (
    <header
      className={`sticky top-0 z-50 bg-white border-b border-[#E5E7EB] transition-shadow ${scrolled ? 'shadow-sm' : ''}`}
      style={{ height: 72 }}
    >
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8 h-full flex items-center justify-between gap-6">
        {/* Logo */}
        <Link to="/" className="flex-shrink-0">
          <img
            src={primaryLight}
            width={160}
            height="auto"
            alt="SkillProof"
            style={{ objectFit: 'contain', display: 'block' }}
          />
        </Link>

        {/* Center nav — desktop */}
        <nav className="hidden md:flex items-center gap-6">
          {navLinks.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              className="text-sm font-medium text-[#17181C] hover:text-[#FF4F8B] transition-colors"
            >
              {link.label}
            </Link>
          ))}
        </nav>

        {/* Right actions — desktop */}
        <div className="hidden md:flex items-center gap-2">
          <Button variant="ghost" size="sm" asChild>
            <Link to="/verify">Verify Certificate</Link>
          </Button>
          <Button variant="outline" size="sm" asChild>
            <Link to="/login">Log In</Link>
          </Button>
          <Button variant="primary" size="sm" asChild>
            <Link to="/register">Get Started</Link>
          </Button>
        </div>

        {/* Hamburger — mobile */}
        <button
          className="md:hidden p-2 rounded-lg text-[#17181C] hover:bg-[#F7F8FA] transition-colors"
          onClick={() => setMobileOpen((v) => !v)}
          aria-label="Toggle menu"
        >
          {mobileOpen ? <X size={20} /> : <Menu size={20} />}
        </button>
      </div>

      {/* Mobile dropdown */}
      {mobileOpen && (
        <div className="md:hidden bg-white border-t border-[#E5E7EB] px-4 py-4 flex flex-col gap-1">
          {navLinks.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              className="py-2 text-sm font-medium text-[#17181C] hover:text-[#FF4F8B] transition-colors"
              onClick={() => setMobileOpen(false)}
            >
              {link.label}
            </Link>
          ))}
          <div className="pt-3 flex flex-col gap-2 border-t border-[#E5E7EB] mt-2">
            <Link to="/verify" className="text-sm font-medium text-[#666A73] py-1">Verify Certificate</Link>
            <Link to="/login" className="text-sm font-medium text-[#17181C] py-1">Log In</Link>
            <Link to="/register">
              <Button variant="primary" size="sm" className="w-full justify-center">Get Started</Button>
            </Link>
          </div>
        </div>
      )}
    </header>
  );
}
