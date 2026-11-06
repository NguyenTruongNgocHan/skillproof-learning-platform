import { useCallback, useState } from "react"
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface"
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider"
import { useAction, useRemote } from "@/shared/hooks/useRemote"
import { ActionNotice, RemoteState } from "@/shared/ui/RemoteState"
import Button from "@/shared/ui/Button"
import Input from "@/shared/ui/Input"
import Pagination from "@/shared/ui/Pagination"
import WorkspaceDialog from "@/features/organization/components/WorkspaceDialog"
import { quizApi } from "../api/quizApi"
import { questionBankApi } from "../api/questionBankApi"
import { QuestionEditor } from "../components/QuestionEditor"
import { QuestionHistory } from "../components/QuestionHistory"
import type { Bank } from "../types/quiz.types"
export default function QuestionBankPage() {
  const { organization, confirmDiscard } = useOrganizationContext()
  const org = organization?.id ?? ""
  const [bankQuery, setBankQuery] = useState("")
  const [questionQuery, setQuestionQuery] = useState("")
  const [includeArchived, setIncludeArchived] = useState(false)
  const [bankPage, setBankPage] = useState(0)
  const [questionPage, setQuestionPage] = useState(0)
  const banks = useRemote(
    useCallback(
      () => questionBankApi.banks(org, bankQuery, includeArchived, bankPage),
      [org, bankQuery, includeArchived, bankPage],
    ),
  )
  const [chosen, setChosen] = useState<Bank | null>(null)
  const bank = banks.data?.content.find((b) => b.id === chosen?.id) ?? chosen
  const questions = useRemote(
    useCallback(
      () =>
        bank
          ? questionBankApi.questions(bank.id, questionQuery, includeArchived, questionPage)
          : Promise.resolve(null),
      [bank, questionQuery, includeArchived, questionPage],
    ),
  )
  const [editor, setEditor] = useState<{ id?: string } | null>(null)
  const [editorBusy, setEditorBusy] = useState(false)
  const [history, setHistory] = useState("")
  const [dialog, setDialog] = useState<"create" | "rename" | null>(null)
  const [title, setTitle] = useState("")
  const action = useAction(() => {
    banks.reload()
    questions.reload()
  })
  return (
    <OrganizerSurface
      title="Question banks"
      description="Reusable questions with immutable revisions and archive history."
      authority="MANAGE_CONTENT"
    >
      <ActionNotice {...action} />
      <div className="sporg-toolbar">
        <Input
          label="Search all banks"
          value={bankQuery}
          onChange={(e) => {
            setBankQuery(e.target.value)
            setBankPage(0)
          }}
        />
        <label className="sporg-checkbox">
          <input
            type="checkbox"
            checked={includeArchived}
            onChange={(e) => {
              setIncludeArchived(e.target.checked)
              setBankPage(0)
              setQuestionPage(0)
            }}
          />
          Include archived
        </label>
        <Button
          onClick={() => {
            setDialog("create")
            setTitle("")
          }}
        >
          Create bank
        </Button>
      </div>
      <RemoteState {...banks} retry={banks.reload} />
      <div className="sporg-bank-workspace">
        <aside className="sporg-card">
          <h2>Banks</h2>
          {banks.data?.content.length === 0 && <p>No matching banks.</p>}
          {banks.data?.content.map((b) => (
            <button
              key={b.id}
              className="sporg-bank-button"
              aria-pressed={bank?.id === b.id}
              onClick={() => {
                if (confirmDiscard()) {
                  setChosen(b)
                  setQuestionPage(0)
                  setQuestionQuery("")
                }
              }}
            >
              {b.title}
              {b.archived && " · Archived"}
            </button>
          ))}
          {banks.data && (
            <Pagination
              {...banks.data}
              page={bankPage}
              busy={banks.loading}
              onChange={setBankPage}
            />
          )}
        </aside>
        <section className="sporg-card">
          <h2>{bank?.title ?? "Select a bank"}</h2>
          {bank && (
            <>
              <p>
                {bank.archived
                  ? "Archived bank: history remains readable; authoring is disabled."
                  : "Active bank"}
              </p>
              {!bank.archived && (
                <div className="sporg-actions">
                  <Button
                    variant="outline"
                    onClick={() => {
                      setTitle(bank.title)
                      setDialog("rename")
                    }}
                  >
                    Rename bank
                  </Button>
                  <Button
                    variant="outline"
                    disabled={action.busy}
                    onClick={() => {
                      if (
                        window.confirm(
                          "Archive this bank? Its questions become read-only and existing snapshots are retained.",
                        )
                      )
                        void action.run(async () => {
                          await questionBankApi.archiveBank(bank.id)
                          setChosen({ ...bank, archived: true })
                        }, "Bank archived.")
                    }}
                  >
                    Archive bank
                  </Button>
                  <Button onClick={() => setEditor({})}>Add question</Button>
                </div>
              )}
              <Input
                label="Search all questions in this bank"
                value={questionQuery}
                onChange={(e) => {
                  setQuestionQuery(e.target.value)
                  setQuestionPage(0)
                }}
              />
              <RemoteState {...questions} retry={questions.reload} />
              {questions.data?.content.length === 0 && <p>No matching questions.</p>}
              {questions.data?.content.map((q) => (
                <div className="sporg-row" key={q.id}>
                  <div>
                    <strong>{q.stem}</strong>
                    <small>
                      Revision {q.version_no}
                      {q.archived && " · Archived"}
                    </small>
                  </div>
                  <div className="sporg-actions">
                    <Button variant="outline" onClick={() => setHistory(q.id)}>
                      Revision history
                    </Button>
                    {!bank.archived && !q.archived && (
                      <>
                        <Button variant="outline" onClick={() => setEditor({ id: q.id })}>
                          Create revision
                        </Button>
                        <Button
                          variant="ghost"
                          disabled={action.busy}
                          onClick={() => {
                            if (
                              window.confirm(
                                "Archive this question? Existing assessment snapshots remain intact.",
                              )
                            )
                              void action.run(
                                () => questionBankApi.archiveQuestion(q.id),
                                "Question archived.",
                              )
                          }}
                        >
                          Archive question
                        </Button>
                      </>
                    )}
                  </div>
                </div>
              ))}
              {questions.data && (
                <Pagination
                  {...questions.data}
                  page={questionPage}
                  busy={questions.loading}
                  onChange={setQuestionPage}
                />
              )}
            </>
          )}
        </section>
      </div>
      {dialog && (
        <WorkspaceDialog
          title={dialog === "create" ? "Create question bank" : "Rename question bank"}
          busy={action.busy}
          close={() => setDialog(null)}
        >
          <form
            className="sporg-form"
            onSubmit={(e) => {
              e.preventDefault()
              void action.run(async () => {
                const b =
                  dialog === "create"
                    ? await quizApi.createBank(org, title.trim())
                    : await questionBankApi.rename(bank!.id, title.trim())
                setChosen(b)
                setDialog(null)
              }, "Question bank saved.")
            }}
          >
            <Input
              label="Bank title"
              required
              maxLength={180}
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
            <ActionNotice {...action} />
            <Button type="submit" disabled={action.busy || !title.trim()}>
              Save bank
            </Button>
          </form>
        </WorkspaceDialog>
      )}
      {editor && bank && (
        <WorkspaceDialog
          busy={editorBusy}
          title={editor.id ? "Create question revision" : "Add question"}
          close={() => setEditor(null)}
        >
          <QuestionEditor
            bankId={bank.id}
            questionId={editor.id}
            onBusyChange={setEditorBusy}
            saved={() => {
              questions.reload()
              setEditor(null)
            }}
          />
        </WorkspaceDialog>
      )}
      {history && (
        <WorkspaceDialog title="Question revision history" close={() => setHistory("")}>
          <QuestionHistory questionId={history} />
        </WorkspaceDialog>
      )}
    </OrganizerSurface>
  )
}
