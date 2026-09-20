import { ArrowRight, CheckCircle2, Play, ShieldCheck, Sparkles } from 'lucide-react';
import { Link } from 'react-router-dom';
import Button from '@/components/ui/Button';
import Container from '@/components/ui/Container';

export default function HeroSection() {
  return <section className="marketing-hero">
    <div className="hero-glow hero-glow--one" /><div className="hero-glow hero-glow--two" />
    <Container>
      <div className="hero-layout">
        <div className="hero-copy">
          <div className="hero-kicker"><Sparkles size={14} /> Learning evidence, not empty claims</div>
          <h1>Build real skills.<br /><span>Make them undeniable.</span></h1>
          <p>SkillProof connects structured learning, meaningful assessment, and independently verifiable credentials in one trusted journey.</p>
          <div className="hero-actions"><Link to="/register"><Button variant="primary" size="lg">Start your journey <ArrowRight size={17} /></Button></Link><a href="#how-it-works" className="hero-text-action"><span><Play size={14} fill="currentColor" /></span>See how it works</a></div>
          <div className="hero-proof">{['Role-aware experience', 'Secure identity flow', 'Verifiable outcomes'].map(item => <span key={item}><CheckCircle2 size={15} />{item}</span>)}</div>
        </div>
        <div className="hero-product" aria-label="SkillProof learning progress preview">
          <div className="hero-product__top"><div><span>Current learning path</span><h2>Backend Engineering</h2></div><div className="verified-pill"><ShieldCheck size={15} /> Verified</div></div>
          <div className="learning-ring"><div><strong>68%</strong><span>complete</span></div></div>
          <div className="hero-product__content"><div className="module-row module-row--done"><CheckCircle2 /><span><b>Programming foundations</b><small>Evidence captured</small></span></div><div className="module-row module-row--done"><CheckCircle2 /><span><b>API design & REST</b><small>Assessment passed</small></span></div><div className="module-row module-row--active"><span className="module-pulse" /><span><b>Cloud fundamentals</b><small>Continue where you left off</small></span><ArrowRight size={16} /></div></div>
          <div className="floating-badge floating-badge--top"><Sparkles size={16} /><span><b>AI guidance</b><small>Personalized next step</small></span></div>
          <div className="floating-badge floating-badge--bottom"><ShieldCheck size={16} /><span><b>Proof ready</b><small>Portable credential</small></span></div>
        </div>
      </div>
    </Container>
  </section>;
}
