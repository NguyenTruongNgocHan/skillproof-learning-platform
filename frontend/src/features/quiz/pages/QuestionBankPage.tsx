import { useCallback, useState } from "react";
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { useAction, useRemote } from "@/shared/hooks/useRemote";
import { ActionNotice, RemoteState } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
import WorkspaceDialog from "@/features/organization/components/WorkspaceDialog";
import { quizApi } from "../api/quizApi";
import { QuestionEditor } from "../components/QuestionEditor";
export default function QuestionBankPage() {
  const [editorBusy, setEditorBusy] = useState(false);
  const { organization } = useOrganizationContext();
  const org = organization?.id ?? "";
  const banks = useRemote(useCallback(() => quizApi.banks(org), [org]));
  const [chosen, setChosen] = useState("");
  const bank =
    banks.data?.find((b) => b.id === chosen)?.id ?? banks.data?.[0]?.id ?? "";
  const questions = useRemote(
    useCallback(
      () => (bank ? quizApi.questions(bank) : Promise.resolve([])),
      [bank],
    ),
  );
  const [creating, setCreating] = useState(false),
    [bankName, setBankName] = useState(""),
    [editor, setEditor] = useState<{ id?: string } | null>(null);
  const [search, setSearch] = useState("");
  const action = useAction(banks.reload);
  return (
    <OrganizerSurface
      title="Question banks"
      description="Build reusable questions with clear ownership and immutable revisions."
      authority="MANAGE_CONTENT"
    >
      <div className="sporg-toolbar">
        <input
          aria-label="Search questions"
          placeholder="Search questions…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <Button onClick={() => setCreating(true)}>Create bank</Button>
      </div>
      <RemoteState {...banks} retry={banks.reload} />
      <div className="sporg-bank-workspace">
        <aside className="sporg-card">
          <h2>Banks</h2>
          {banks.data?.length === 0 && (
            <p>No banks yet. Create one to organize questions.</p>
          )}
          {banks.data?.map((b) => (
            <button
              className="sporg-bank-button"
              key={b.id}
              aria-pressed={bank === b.id}
              onClick={() => {
                setChosen(b.id);
                setSearch("");
              }}
            >
              {b.title}
            </button>
          ))}
        </aside>
        <section className="sporg-card">
          <div className="sporg-toolbar">
            <h2>
              {banks.data?.find((b) => b.id === bank)?.title ?? "Questions"}
            </h2>
            <Button
              variant="outline"
              disabled={!bank}
              onClick={() => setEditor({})}
            >
              Add question
            </Button>
          </div>
          <RemoteState
            loading={Boolean(bank) && questions.loading}
            error={questions.error}
            retry={questions.reload}
          />
          {bank && questions.data?.length === 0 && (
            <div className="sporg-empty">
              <h3>Add your first question</h3>
              <p>
                Each question needs at least two options and one correct answer.
              </p>
            </div>
          )}
          {questions.data
            ?.filter((q) => q.stem.toLowerCase().includes(search.toLowerCase()))
            .map((q) => (
              <div className="sporg-row" key={q.id}>
                <div>
                  <strong>{q.stem}</strong>
                  <small>Revision {q.version_no}</small>
                </div>
                <Button
                  variant="outline"
                  onClick={() => setEditor({ id: q.id })}
                >
                  Create revision
                </Button>
              </div>
            ))}
        </section>
      </div>
      {creating && (
        <WorkspaceDialog
          title="Create question bank"
          busy={action.busy}
          close={() => setCreating(false)}
        >
          <form
            className="sporg-form"
            onSubmit={(e) => {
              e.preventDefault();
              void action.run(async () => {
                const b = await quizApi.createBank(org, bankName.trim());
                setChosen(b.id);
                setBankName("");
                setCreating(false);
              }, "Question bank created.");
            }}
          >
            <Input
              label="Bank name"
              required
              maxLength={180}
              value={bankName}
              onChange={(e) => setBankName(e.target.value)}
            />
            <ActionNotice {...action} />
            <Button type="submit" disabled={action.busy}>
              Create bank
            </Button>
          </form>
        </WorkspaceDialog>
      )}
      {editor && (
        <WorkspaceDialog
          busy={editorBusy}
          title={editor.id ? "Create question revision" : "Add question"}
          close={() => {
            if (window.confirm("Discard this question editor?"))
              setEditor(null);
          }}
        >
          <QuestionEditor
            onBusyChange={setEditorBusy}
            bankId={bank}
            questionId={editor.id}
            saved={() => {
              questions.reload();
              setEditor(null);
            }}
          />
        </WorkspaceDialog>
      )}
    </OrganizerSurface>
  );
}
