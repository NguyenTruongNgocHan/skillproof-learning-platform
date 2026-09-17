import { Link } from 'react-router-dom';
import { wordmarkDark } from '@/assets/brand';

const columns = [
  {
    heading: 'Product',
    links: [
      { label: 'Learning Paths', to: '/learning-paths' },
      { label: 'Practice', to: '/practice' },
      { label: 'Realtime Challenge', to: '/practice' },
      { label: 'Certifications', to: '/certifications' },
      { label: 'Community', to: '/community' },
    ],
  },
  {
    heading: 'For Organizations',
    links: [
      { label: 'Organizer Platform', to: '/organizer' },
      { label: 'Certification', to: '/organizer' },
      { label: 'Credential Verification', to: '/verify' },
    ],
  },
  {
    heading: 'Resources',
    links: [
      { label: 'About', to: '/about' },
      { label: 'Documentation', to: '/docs' },
      { label: 'Help Center', to: '/help' },
    ],
  },
  {
    heading: 'Legal',
    links: [
      { label: 'Privacy', to: '/privacy' },
      { label: 'Terms', to: '/terms' },
    ],
  },
];

export default function PublicFooter() {
  return (
    <footer style={{ backgroundColor: '#111318' }} className="text-white">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="grid grid-cols-2 md:grid-cols-5 gap-10 mb-12">
          {/* Brand column */}
          <div className="col-span-2 md:col-span-1">
            <img
              src={wordmarkDark}
              width={140}
              height="auto"
              alt="SkillProof"
              style={{ objectFit: 'contain', display: 'block' }}
            />
            <p className="mt-3 text-sm text-gray-400">Learn • Practice • Prove</p>
          </div>

          {/* Link columns */}
          {columns.map((col) => (
            <div key={col.heading}>
              <h3 className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-4">
                {col.heading}
              </h3>
              <ul className="space-y-2">
                {col.links.map((link) => (
                  <li key={link.label}>
                    <Link
                      to={link.to}
                      className="text-sm text-gray-300 hover:text-white transition-colors"
                    >
                      {link.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="border-t border-gray-800 pt-6 flex flex-col sm:flex-row justify-between items-center gap-3 text-sm text-gray-500">
          <p>© {new Date().getFullYear()} SkillProof. All rights reserved.</p>
          <p>Built for learners and organizations that take credentials seriously.</p>
        </div>
      </div>
    </footer>
  );
}
