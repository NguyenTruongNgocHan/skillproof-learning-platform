import { useState } from "react"
import { ArrowRight, BriefcaseBusiness, Code2, Languages, Palette, PieChart, Sparkles } from "lucide-react"
import InteractiveCard from "./InteractiveCard"

const categories = [
  { label: "Languages", icon: Languages, examples: ["Japanese", "English", "Communication"] },
  { label: "Business", icon: BriefcaseBusiness, examples: ["Marketing", "Management", "Strategy"] },
  { label: "Technology", icon: Code2, examples: ["Backend", "Cloud", "Mobile"] },
  { label: "Design", icon: Palette, examples: ["UI/UX", "Product", "Visual design"] },
  { label: "Finance", icon: PieChart, examples: ["Finance", "Analytics", "Economics"] },
]

interface SkillExplorerProps {
  onSelect: (value: string) => void
  onMore: () => void
}

export default function SkillExplorer({ onSelect, onMore }: SkillExplorerProps) {
  const [active, setActive] = useState<string | null>(null)

  return (
    <div className="skill-explorer-v3">
      <div className="skill-explorer-v3__rail">
        {categories.map(({ label, icon: Icon, examples }) => (
          <InteractiveCard key={label} className={`skill-category${active === label ? " is-active" : ""}`}>
            <button type="button" className="skill-category__button" onClick={() => setActive(active === label ? null : label)}>
              <span className="skill-category__icon"><Icon size={20} /></span>
              <strong>{label}</strong>
              <span className="skill-category__hint">Explore</span>
            </button>
            <div className="skill-category__preview" aria-hidden={active !== label}>
              {examples.map((example) => (
                <button key={example} type="button" onClick={() => onSelect(example)}>{example}</button>
              ))}
              <button type="button" className="skill-category__all" onClick={() => onSelect(label)}>
                Explore {label.toLowerCase()} <ArrowRight size={14} />
              </button>
            </div>
          </InteractiveCard>
        ))}

        <InteractiveCard className="skill-category skill-category--more">
          <button type="button" className="skill-category__button" onClick={onMore}>
            <span className="skill-category__icon"><Sparkles size={20} /></span>
            <strong>More</strong>
            <span className="skill-category__hint">See all</span>
          </button>
        </InteractiveCard>
      </div>
    </div>
  )
}
