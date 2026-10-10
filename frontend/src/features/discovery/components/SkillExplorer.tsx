import InteractiveCard from "@/features/discovery/components/InteractiveCard"
import {
  ArrowRight,
  BriefcaseBusiness,
  Code2,
  Languages,
  Palette,
  PieChart,
  Sparkles,
} from "lucide-react"
import { useState } from "react"

const categories = [
  {
    label: "Languages",
    icon: Languages,
    number: "01",
    examples: ["Japanese", "English", "Communication"],
  },
  {
    label: "Business",
    icon: BriefcaseBusiness,
    number: "02",
    examples: ["Marketing", "Management", "Strategy"],
  },
  {
    label: "Technology",
    icon: Code2,
    number: "03",
    examples: ["Backend", "Cloud", "Mobile"],
  },
  {
    label: "Design",
    icon: Palette,
    number: "04",
    examples: ["UI/UX", "Product", "Visual design"],
  },
  {
    label: "Finance",
    icon: PieChart,
    number: "05",
    examples: ["Finance", "Analytics", "Economics"],
  },
]
interface SkillExplorerProps {
  onSelect: (value: string) => void
  onMore: () => void
}

export default function SkillExplorer({ onSelect, onMore }: SkillExplorerProps) {
  const [active, setActive] = useState<string | null>(null)
  return (
    <div className="skill-explorer-v31">
      <div className="skill-explorer-v31__rail">
        {categories.map(({ label, icon: Icon, number, examples }) => (
          <InteractiveCard
            key={label}
            className={`skill-category-v31${active === label ? " is-active" : ""}`}
          >
            <button
              type="button"
              className="skill-category-v31__button"
              onClick={() => setActive(active === label ? null : label)}
              aria-expanded={active === label}
            >
              <span className="skill-category-v31__number">{number}</span>
              <span className="skill-category-v31__icon">
                <Icon size={22} />
              </span>
              <strong>{label}</strong>
              <ArrowRight className="skill-category-v31__arrow" size={16} />
            </button>
            <div className="skill-category-v31__preview">
              {examples.map((example) => (
                <button key={example} type="button" onClick={() => onSelect(example)}>
                  {example}
                </button>
              ))}
              <button
                type="button"
                className="skill-category-v31__all"
                onClick={() => onSelect(label)}
              >
                Explore all <ArrowRight size={13} />
              </button>
            </div>
          </InteractiveCard>
        ))}
        <InteractiveCard className="skill-category-v31 skill-category-v31--more">
          <button type="button" className="skill-category-v31__button" onClick={onMore}>
            <span className="skill-category-v31__number">06</span>
            <span className="skill-category-v31__icon">
              <Sparkles size={22} />
            </span>
            <strong>More</strong>
            <ArrowRight className="skill-category-v31__arrow" size={16} />
          </button>
        </InteractiveCard>
      </div>
    </div>
  )
}
