import MediaPanel from "@/features/media/MediaPanel"
import Button from "@/components/ui/Button"
import { learningApi, type Resource } from "@/features/learning/learningApi"
import { quizApi, type Kind } from "@/features/quiz/quizApi"
import { useStudioState } from "@/features/learning/PathStudioContext"

export default function JourneySection() {
  const {
    version,
    id,
    modules,
    busy,
    moduleTitle,
    editingModule,
    resourceModule,
    editingResource,
    resourceKind,
    resourceTitle,
    resourceBody,
    kind,
    act,
    addModule,
    addResource,
    setModuleTitle,
    setEditingModule,
    setResourceModule,
    setEditingResource,
    setResourceKind,
    setResourceTitle,
    setResourceBody,
  } = useStudioState()
  function moved(ids: string[], index: number, direction: number) {
    const next = [...ids]
    ;[next[index], next[index + direction]] = [
      next[index + direction],
      next[index],
    ]
    return next
  }
  if (!version) return null
  const draft = version.status === "DRAFT"
  return (
    <section className="v7-section">
      <h2>1. Build the learning journey</h2>
      {draft && (
        <form className="v7-form v7-card" onSubmit={(e) => void addModule(e)}>
          <label>
            {editingModule ? "Edit module title" : "Module title"}
            <input
              required
              maxLength={180}
              value={moduleTitle}
              onChange={(e) => setModuleTitle(e.target.value)}
              placeholder="e.g. Foundations"
            />
          </label>
          <Button type="submit" disabled={busy}>
            {editingModule ? "Save module" : "Add module"}
          </Button>
          {editingModule && (
            <Button
              variant="ghost"
              onClick={() => {
                setEditingModule(null)
                setModuleTitle("")
              }}
            >
              Cancel
            </Button>
          )}
        </form>
      )}
      {modules.length === 0 ? (
        <p>
          Start with a module. Publish needs at least one learning resource.
        </p>
      ) : (
        modules.map((m, moduleIndex) => (
          <article className="v7-card" key={m.id}>
            <div className="v7-inline">
              <h3>
                {m.position}. {m.title}
              </h3>
              {draft && (
                <>
                  <Button
                    variant="outline"
                    disabled={busy || moduleIndex === 0}
                    onClick={() =>
                      void act(
                        () =>
                          learningApi.reorderModules(
                            version.id,
                            moved(
                              modules.map((x) => x.id),
                              moduleIndex,
                              -1,
                            ),
                          ),
                        "Module order saved.",
                      )
                    }
                  >
                    Move up
                  </Button>
                  <Button
                    variant="outline"
                    disabled={busy || moduleIndex === modules.length - 1}
                    onClick={() =>
                      void act(
                        () =>
                          learningApi.reorderModules(
                            version.id,
                            moved(
                              modules.map((x) => x.id),
                              moduleIndex,
                              1,
                            ),
                          ),
                        "Module order saved.",
                      )
                    }
                  >
                    Move down
                  </Button>
                </>
              )}
              {draft && (
                <Button
                  variant="outline"
                  disabled={busy}
                  onClick={() => {
                    setEditingModule(m)
                    setModuleTitle(m.title)
                  }}
                >
                  Rename
                </Button>
              )}
              {draft && (
                <Button
                  variant="ghost"
                  disabled={busy}
                  onClick={() => {
                    if (
                      window.confirm(
                        `Delete ${m.title} and its draft resources?`,
                      )
                    )
                      void act(
                        () => learningApi.deleteModule(m.id),
                        "Module removed.",
                      )
                  }}
                >
                  Delete
                </Button>
              )}
            </div>
            {m.resources.length === 0 ? (
              <p>No resources in this module.</p>
            ) : (
              m.resources.map((r, resourceIndex) => (
                <div className="v7-row" key={r.id}>
                  <span>
                    {r.position}. {r.title} · {r.kind}
                  </span>
                  <MediaPanel
                    scope="RESOURCE"
                    target={r.id}
                    writable={draft}
                    removable
                  />
                  {draft && (
                    <div className="v7-inline">
                      <Button
                        variant="outline"
                        disabled={busy || resourceIndex === 0}
                        onClick={() =>
                          void act(
                            () =>
                              learningApi.reorderResources(
                                m.id,
                                moved(
                                  m.resources.map((x) => x.id),
                                  resourceIndex,
                                  -1,
                                ),
                              ),
                            "Resource order saved.",
                          )
                        }
                      >
                        Move up
                      </Button>
                      <Button
                        variant="outline"
                        disabled={
                          busy || resourceIndex === m.resources.length - 1
                        }
                        onClick={() =>
                          void act(
                            () =>
                              learningApi.reorderResources(
                                m.id,
                                moved(
                                  m.resources.map((x) => x.id),
                                  resourceIndex,
                                  1,
                                ),
                              ),
                            "Resource order saved.",
                          )
                        }
                      >
                        Move down
                      </Button>
                      <Button
                        variant="outline"
                        disabled={busy}
                        onClick={() => {
                          setEditingResource(r)
                          setResourceModule(m.id)
                          setResourceKind(r.kind)
                          setResourceTitle(r.title)
                          setResourceBody(
                            r.kind === "ARTICLE"
                              ? (r.body ?? "")
                              : (r.url ?? ""),
                          )
                        }}
                      >
                        Edit
                      </Button>
                      <Button
                        variant="ghost"
                        disabled={busy}
                        onClick={() => {
                          if (window.confirm(`Remove ${r.title}?`))
                            void act(
                              () => learningApi.deleteResource(r.id),
                              "Resource removed.",
                            )
                        }}
                      >
                        Remove
                      </Button>
                    </div>
                  )}
                </div>
              ))
            )}
          </article>
        ))
      )}
      {draft && modules.length > 0 && (
        <form className="v7-form v7-card" onSubmit={(e) => void addResource(e)}>
          <h3>
            {editingResource ? "Edit resource" : "Add a learning resource"}
          </h3>
          <label>
            Module
            <select
              value={resourceModule}
              required
              disabled={!!editingResource}
              onChange={(e) => setResourceModule(e.target.value)}
            >
              <option value="">Choose a module</option>
              {modules.map((m, moduleIndex) => (
                <option value={m.id} key={m.id}>
                  {m.title}
                </option>
              ))}
            </select>
          </label>
          <label>
            Format
            <select
              value={resourceKind}
              onChange={(e) => {
                setResourceKind(e.target.value as Resource["kind"])
                setResourceBody("")
              }}
            >
              <option value="ARTICLE">Article</option>
              <option value="LINK">Link</option>
              <option value="VIDEO">Video link</option>
              <option value="FILE">Uploaded document / image / video</option>
              <option value="AUDIO">Uploaded audio</option>
            </select>
          </label>
          <label>
            Resource title
            <input
              required
              maxLength={180}
              value={resourceTitle}
              onChange={(e) => setResourceTitle(e.target.value)}
            />
          </label>
          {resourceKind !== "FILE" && resourceKind !== "AUDIO" && (
            <label>
              {resourceKind === "ARTICLE"
                ? "Lesson text"
                : "HTTPS resource URL"}
              {resourceKind === "ARTICLE" ? (
                <textarea
                  required
                  value={resourceBody}
                  onChange={(e) => setResourceBody(e.target.value)}
                />
              ) : (
                <input
                  required
                  type="url"
                  pattern="https://.*"
                  value={resourceBody}
                  onChange={(e) => setResourceBody(e.target.value)}
                />
              )}
            </label>
          )}
          {(resourceKind === "FILE" || resourceKind === "AUDIO") && (
            <p>
              Save the resource, then upload its file in the module list above.
              Publishing requires an attachment.
            </p>
          )}
          <Button type="submit" disabled={busy}>
            {editingResource ? "Save resource" : "Add resource"}
          </Button>
          {editingResource && (
            <Button
              variant="ghost"
              onClick={() => {
                setEditingResource(null)
                setResourceTitle("")
                setResourceBody("")
              }}
            >
              Cancel edit
            </Button>
          )}
        </form>
      )}
    </section>
  )
}
