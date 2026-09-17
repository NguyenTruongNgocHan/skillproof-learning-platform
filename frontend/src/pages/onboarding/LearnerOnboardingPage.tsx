import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CheckCircle, Circle } from 'lucide-react';
import { useAuth } from '@/features/auth/hooks/useAuth';
import Button from '@/components/ui/Button';

const CAREER_GOALS = [
  'Backend Developer',
  'Frontend Developer',
  'Full-Stack Developer',
  'Data Engineer',
  'DevOps Engineer',
  'QA / Test Engineer',
  'Mobile Developer',
];

const TARGET_ROLES = [
  'Junior Developer',
  'Mid-level Developer',
  'Senior Developer',
  'Tech Lead',
  'Software Architect',
  'Engineering Manager',
  'Data Scientist',
  'Cloud Engineer',
  'Security Engineer',
];

const SKILL_LEVELS = [
  {
    value: 'Beginner',
    description: "I'm new to programming or just starting out",
  },
  {
    value: 'Intermediate',
    description: 'I have some experience and can build basic projects',
  },
  {
    value: 'Advanced',
    description: "I'm experienced and looking to level up further",
  },
];

const WEEKLY_GOALS = ['1-2 hours', '3-5 hours', '6-10 hours', '10+ hours'];

const LEARNING_METHODS = [
  'Video lessons',
  'Reading & documentation',
  'Hands-on practice',
  'Quizzes & assessments',
];

interface OptionCardProps {
  label: string;
  selected: boolean;
  onClick: () => void;
}

function OptionCard({ label, selected, onClick }: OptionCardProps) {
  return (
    <button
      onClick={onClick}
      className="w-full text-left rounded-lg p-3.5 cursor-pointer transition-all text-sm font-medium"
      style={{
        border: `1.5px solid ${selected ? 'var(--brand)' : 'var(--border)'}`,
        background: selected ? 'var(--brand-soft)' : 'var(--surface)',
        color: 'var(--fg)',
      }}
    >
      {label}
    </button>
  );
}

interface ProgressDotsProps {
  total: number;
  current: number;
}

function ProgressDots({ total, current }: ProgressDotsProps) {
  return (
    <div className="flex items-center gap-2 justify-center mb-8">
      {Array.from({ length: total }, (_, i) => {
        const step = i + 1;
        const filled = step <= current;
        return filled ? (
          <CheckCircle key={i} size={20} style={{ color: 'var(--brand)' }} />
        ) : (
          <Circle key={i} size={20} style={{ color: 'var(--border)' }} />
        );
      })}
    </div>
  );
}

export default function LearnerOnboardingPage() {
  const { completeLearnerOnboarding } = useAuth();
  const navigate = useNavigate();

  const [currentStep, setCurrentStep] = useState(1);
  const [careerGoal, setCareerGoal] = useState('');
  const [targetRole, setTargetRole] = useState('');
  const [skillLevel, setSkillLevel] = useState('');
  const [weeklyGoal, setWeeklyGoal] = useState('');
  const [learningMethods, setLearningMethods] = useState<string[]>([]);

  const toggleMethod = (method: string) => {
    setLearningMethods((prev) =>
      prev.includes(method) ? prev.filter((m) => m !== method) : [...prev, method]
    );
  };

  const handleFinish = async () => {
    await completeLearnerOnboarding();
    navigate('/app');
  };

  return (
    <div
      className="min-h-screen flex items-center justify-center px-4 py-12"
      style={{ background: 'var(--bg)' }}
    >
      <div
        className="w-full max-w-lg rounded-2xl shadow p-8"
        style={{ background: 'var(--surface)' }}
      >
        <ProgressDots total={4} current={currentStep} />

        {/* STEP 1 */}
        {currentStep === 1 && (
          <div className="flex flex-col gap-5">
            <h1 className="text-xl font-bold" style={{ color: 'var(--fg)' }}>
              What is your career goal?
            </h1>
            <div className="flex flex-col gap-2">
              {CAREER_GOALS.map((goal) => (
                <OptionCard
                  key={goal}
                  label={goal}
                  selected={careerGoal === goal}
                  onClick={() => setCareerGoal(goal)}
                />
              ))}
            </div>
            <Button
              variant="primary"
              size="lg"
              className="w-full mt-2"
              disabled={!careerGoal}
              onClick={() => setCurrentStep(2)}
            >
              Next
            </Button>
          </div>
        )}

        {/* STEP 2 */}
        {currentStep === 2 && (
          <div className="flex flex-col gap-5">
            <h1 className="text-xl font-bold" style={{ color: 'var(--fg)' }}>
              What role are you targeting?
            </h1>
            <div className="flex flex-col gap-2">
              {TARGET_ROLES.map((role) => (
                <OptionCard
                  key={role}
                  label={role}
                  selected={targetRole === role}
                  onClick={() => setTargetRole(role)}
                />
              ))}
            </div>
            <div className="flex gap-3 mt-2">
              <Button variant="outline" size="lg" className="flex-1" onClick={() => setCurrentStep(1)}>
                Back
              </Button>
              <Button
                variant="primary"
                size="lg"
                className="flex-1"
                disabled={!targetRole}
                onClick={() => setCurrentStep(3)}
              >
                Next
              </Button>
            </div>
          </div>
        )}

        {/* STEP 3 */}
        {currentStep === 3 && (
          <div className="flex flex-col gap-5">
            <h1 className="text-xl font-bold" style={{ color: 'var(--fg)' }}>
              What is your current skill level?
            </h1>
            <div className="flex flex-col gap-3">
              {SKILL_LEVELS.map((level) => (
                <button
                  key={level.value}
                  onClick={() => setSkillLevel(level.value)}
                  className="w-full text-left rounded-lg p-4 cursor-pointer transition-all"
                  style={{
                    border: `1.5px solid ${skillLevel === level.value ? 'var(--brand)' : 'var(--border)'}`,
                    background: skillLevel === level.value ? 'var(--brand-soft)' : 'var(--surface)',
                  }}
                >
                  <div className="font-semibold text-sm" style={{ color: 'var(--fg)' }}>
                    {level.value}
                  </div>
                  <div className="text-xs mt-0.5" style={{ color: 'var(--fg-muted)' }}>
                    {level.description}
                  </div>
                </button>
              ))}
            </div>
            <div className="flex gap-3 mt-2">
              <Button variant="outline" size="lg" className="flex-1" onClick={() => setCurrentStep(2)}>
                Back
              </Button>
              <Button
                variant="primary"
                size="lg"
                className="flex-1"
                disabled={!skillLevel}
                onClick={() => setCurrentStep(4)}
              >
                Next
              </Button>
            </div>
          </div>
        )}

        {/* STEP 4 */}
        {currentStep === 4 && (
          <div className="flex flex-col gap-6">
            <h1 className="text-xl font-bold" style={{ color: 'var(--fg)' }}>
              Customize your learning plan
            </h1>

            {/* Weekly goal */}
            <div className="flex flex-col gap-3">
              <p className="text-sm font-medium" style={{ color: 'var(--fg)' }}>
                How many hours per week can you dedicate?
              </p>
              <div className="grid grid-cols-2 gap-2">
                {WEEKLY_GOALS.map((goal) => (
                  <button
                    key={goal}
                    onClick={() => setWeeklyGoal(goal)}
                    className="rounded-lg py-2.5 px-3 text-sm font-medium cursor-pointer transition-all"
                    style={{
                      border: `1.5px solid ${weeklyGoal === goal ? 'var(--brand)' : 'var(--border)'}`,
                      background: weeklyGoal === goal ? 'var(--brand-soft)' : 'var(--surface)',
                      color: 'var(--fg)',
                    }}
                  >
                    {goal}
                  </button>
                ))}
              </div>
            </div>

            {/* Learning methods */}
            <div className="flex flex-col gap-3">
              <p className="text-sm font-medium" style={{ color: 'var(--fg)' }}>
                How do you prefer to learn? <span style={{ color: 'var(--fg-muted)', fontWeight: 400 }}>(select all that apply)</span>
              </p>
              <div className="flex flex-col gap-2">
                {LEARNING_METHODS.map((method) => {
                  const checked = learningMethods.includes(method);
                  return (
                    <label
                      key={method}
                      className="flex items-center gap-3 rounded-lg p-3.5 cursor-pointer transition-all"
                      style={{
                        border: `1.5px solid ${checked ? 'var(--brand)' : 'var(--border)'}`,
                        background: checked ? 'var(--brand-soft)' : 'var(--surface)',
                      }}
                    >
                      <input
                        type="checkbox"
                        checked={checked}
                        onChange={() => toggleMethod(method)}
                        className="w-4 h-4 cursor-pointer"
                        style={{ accentColor: 'var(--brand)' }}
                      />
                      <span className="text-sm font-medium" style={{ color: 'var(--fg)' }}>
                        {method}
                      </span>
                    </label>
                  );
                })}
              </div>
            </div>

            <div className="flex gap-3 mt-2">
              <Button variant="outline" size="lg" className="flex-1" onClick={() => setCurrentStep(3)}>
                Back
              </Button>
              <Button
                variant="primary"
                size="lg"
                className="flex-1"
                disabled={!weeklyGoal || learningMethods.length === 0}
                onClick={handleFinish}
              >
                Build My Learning Plan
              </Button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
