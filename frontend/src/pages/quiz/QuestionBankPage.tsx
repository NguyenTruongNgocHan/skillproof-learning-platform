import { useEffect, useState } from "react"
import AppShell from "@/components/layout/AppShell"
import Button from "@/components/ui/Button"
import { organizationApi } from "@/features/organization/organizationApi"
import { quizApi, type Bank, type Question } from "@/features/quiz/quizApi"
export default function QuestionBankPage() {
  const [org, setOrg] = useState(""),
    [banks, setBanks] = useState<Bank[]>([]),
    [selected, setSelected] = useState(""),
    [questions, setQuestions] = useState<Question[]>([]),
    [title, setTitle] = useState(""),
    [stem, setStem] = useState(""),
    [editing, setEditing] = useState<string | null>(null),
    [choices, setChoices] = useState(["", "", "", ""]),
    [correct, setCorrect] = useState(0),
    [error, setError] = useState(""),
    [loading, setLoading] = useState(true),
    [busy, setBusy] = useState(false)
  async function load() {
    setLoading(true)
    try {
      const o = await organizationApi.mine()
      setOrg(o.id)
      const data = await quizApi.banks(o.id)
      setBanks(data)
      setSelected((previous) => previous || data[0]?.id || "")
      setError("")
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to load question banks")
    } finally {
      setLoading(false)
    }
  }
  useEffect(() => {
    void load()
  }, [])
  useEffect(() => {
    if (!selected) {
      setQuestions([])
      return
    }
    quizApi
      .questions(selected)
      .then(setQuestions)
      .catch((e) =>
        setError(e instanceof Error ? e.message : "Unable to load questions"),
      )
  }, [selected])
  async function createBank(e: React.FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError("")
    try {
      const b = await quizApi.createBank(org, title)
      setTitle("")
      setBanks((prev) => [b, ...prev])
      setSelected(b.id)
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to create bank")
    } finally {
      setBusy(false)
    }
  }
  async function createQuestion(e: React.FormEvent) {
    e.preventDefault()
    if (!selected) return
    setBusy(true)
    setError("")
    try {
      const data = {
        stem,
        options: choices
          .map((body, i) => ({ body, correct: i === correct }))
          .filter((c) => c.body.trim()),
      }
      if (editing) await quizApi.reviseQuestion(editing, data)
      else await quizApi.addQuestion(selected, data)
      setQuestions(await quizApi.questions(selected))
      setStem("")
      setEditing(null)
      setChoices(["", "", "", ""])
      setCorrect(0)
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to save question")
    } finally {
      setBusy(false)
    }
  }
  return (
    <AppShell>
      <div className="v7-wrap">
        <header className="v7-hero">
          <span className="org-eyebrow">QUESTION BANK</span>
          <h1>Questions with a clear owner and version.</h1>
          <p>
            One correct answer per question. Editing a question later creates a
            new version and keeps existing attempts intact.
          </p>
        </header>
        {loading ? (
          <p role="status">Loading question banks…</p>
        ) : error && !org ? (
          <p role="alert" className="org-error">
            {error}{" "}
            <Button variant="outline" onClick={() => void load()}>
              Retry
            </Button>
          </p>
        ) : (
          <div className="v7-grid">
            <section className="v7-card">
              <h2>Question banks</h2>
              <form className="v7-form" onSubmit={(e) => void createBank(e)}>
                <label>
                  Bank name
                  <input
                    required
                    maxLength={180}
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                  />
                </label>
                <Button type="submit" disabled={busy}>
                  Create bank
                </Button>
              </form>
              {banks.map((b) => (
                <button
                  className={`v7-row ${
                    selected === b.id ? "v7-row-active" : ""
                  }`}
                  onClick={() => setSelected(b.id)}
                  key={b.id}
                >
                  {b.title}
                </button>
              ))}
            </section>
            <section className="v7-card">
              <h2>
                {editing
                  ? "Create a new question version"
                  : "Create a question"}
              </h2>
              {error && (
                <p className="org-error" role="alert">
                  {error}
                </p>
              )}
              {!selected ? (
                <p>Select or create a question bank first.</p>
              ) : (
                <>
                  <form
                    className="v7-form"
                    onSubmit={(e) => void createQuestion(e)}
                  >
                    <label>
                      Question prompt
                      <textarea
                        required
                        maxLength={2000}
                        value={stem}
                        onChange={(e) => setStem(e.target.value)}
                      />
                    </label>
                    {choices.map((choice, i) => (
                      <label key={i}>
                        Option {i + 1} {i === correct ? "· correct" : ""}
                        <div className="v7-inline">
                          <input
                            type="radio"
                            name="correct"
                            checked={correct === i}
                            onChange={() => setCorrect(i)}
                            aria-label={`Mark option ${i + 1} correct`}
                          />
                          <input
                            value={choice}
                            onChange={(e) =>
                              setChoices((prev) =>
                                prev.map((v, j) =>
                                  i === j ? e.target.value : v,
                                ),
                              )
                            }
                            maxLength={1000}
                            required={i < 2}
                          />
                        </div>
                      </label>
                    ))}
                    <div className="v7-inline">
                      <Button
                        variant="outline"
                        disabled={choices.length >= 8}
                        onClick={() => setChoices((prev) => [...prev, ""])}
                      >
                        Add option
                      </Button>
                      {choices.length > 2 && (
                        <Button
                          variant="ghost"
                          onClick={() => {
                            setChoices((prev) => prev.slice(0, -1))
                            setCorrect((prev) =>
                              Math.min(prev, choices.length - 2),
                            )
                          }}
                        >
                          Remove last option
                        </Button>
                      )}
                    </div>
                    <Button
                      type="submit"
                      disabled={
                        busy ||
                        choices.filter((c) => c.trim()).length < 2 ||
                        !choices[correct]?.trim()
                      }
                    >
                      {editing ? "Save new version" : "Save question"}
                    </Button>
                    {editing && (
                      <Button
                        variant="ghost"
                        onClick={() => {
                          setEditing(null)
                          setStem("")
                          setChoices(["", "", "", ""])
                          setCorrect(0)
                        }}
                      >
                        Cancel editing
                      </Button>
                    )}
                  </form>
                  <h3>Bank questions</h3>
                  {questions.length === 0 ? (
                    <p>No questions yet.</p>
                  ) : (
                    questions.map((q) => (
                      <div className="v7-row" key={q.id}>
                        <span>{q.stem}</span>
                        <small>
                          Version {q.version_no} · {q.version_id}
                        </small>
                        <Button
                          variant="outline"
                          disabled={busy}
                          onClick={async () => {
                            try {
                              const detail = await quizApi.question(q.id)
                              setEditing(q.id)
                              setStem(detail.version.stem)
                              setChoices([
                                ...detail.options.map((o) => o.body),
                                ...Array(
                                  Math.max(0, 4 - detail.options.length),
                                ).fill(""),
                              ])
                              setCorrect(
                                Math.max(
                                  0,
                                  detail.options.findIndex((o) => o.correct),
                                ),
                              )
                              setError("")
                            } catch (e) {
                              setError(
                                e instanceof Error
                                  ? e.message
                                  : "Could not load question",
                              )
                            }
                          }}
                        >
                          Create revision
                        </Button>
                      </div>
                    ))
                  )}
                </>
              )}
            </section>
          </div>
        )}
      </div>
    </AppShell>
  )
}
