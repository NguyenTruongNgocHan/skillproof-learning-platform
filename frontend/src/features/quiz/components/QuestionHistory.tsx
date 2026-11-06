import { useCallback, useState } from "react"
import { questionBankApi } from "../api/questionBankApi"
import { useRemote } from "@/shared/hooks/useRemote"
import { RemoteState } from "@/shared/ui/RemoteState"
import Button from "@/shared/ui/Button"
export function QuestionHistory({ questionId }: { questionId: string }) {
  const remote = useRemote(useCallback(() => questionBankApi.revisions(questionId), [questionId]))
  const [selected, setSelected] = useState("")
  const preview = useRemote(
    useCallback(
      () => (selected ? questionBankApi.preview(selected) : Promise.resolve(null)),
      [selected],
    ),
  )
  return (
    <div className="platform-stack">
      <RemoteState {...remote} retry={remote.reload} />
      <p>
        Revisions are immutable. Existing assessments retain the question revision attached to them.
      </p>
      {remote.data?.map((v) => (
        <div key={v.id} className="sporg-row">
          <div>
            <strong>Revision {v.versionNo}</strong>
            <p>{v.stem}</p>
            <small>{new Date(v.createdAt).toLocaleString()}</small>
          </div>
          <Button variant="outline" onClick={() => setSelected(v.id)}>
            Preview revision {v.versionNo}
          </Button>
        </div>
      ))}
      {selected && <RemoteState {...preview} retry={preview.reload} />}
      {preview.data && (
        <section className="sporg-card">
          <h3>{preview.data.stem}</h3>
          <ol>
            {preview.data.options.map((o) => (
              <li key={o.id}>
                {o.body}
                {o.correct && <strong> · Correct answer</strong>}
              </li>
            ))}
          </ol>
        </section>
      )}
    </div>
  )
}
