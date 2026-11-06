import { useEffect, useMemo, useState, type ReactNode } from "react"
import { Search, RotateCcw, ChevronLeft, ChevronRight, ArrowDownUp, ListFilter } from "lucide-react"
export type Column<T> = {
  key: string
  label: string
  render: (item: T) => ReactNode
  sortValue?: (item: T) => string | number
}
type Props<T> = {
  rows: T[]
  columns: Column<T>[]
  searchText: (r: T) => string
  onOpen: (r: T) => void
  empty?: string
  pageSize?: number
  serverPage?: number
  serverNext?: boolean
  serverTotal?: number
  onServerPage?: (n: number) => void
  filters?: { key: string; label: string; value: (r: T) => string }[]
}
export default function AdminTable<T extends { id: string }>({
  rows,
  columns,
  searchText,
  onOpen,
  empty = "No records found",
  pageSize = 10,
  serverPage,
  serverNext,
  serverTotal,
  onServerPage,
  filters = [],
}: Props<T>) {
  const [query, setQuery] = useState("")
  const [sort, setSort] = useState("")
  const [direction, setDirection] = useState<"asc" | "desc">("asc")
  const [page, setPage] = useState(0)
  const [filter, setFilter] = useState("")
  const choices = useMemo(
    () =>
      Array.from(
        new Set(
          rows
            .map((r) => filters.find((f) => f.key === filter)?.value(r))
            .filter((v): v is string => Boolean(v)),
        ),
      ).sort(),
    [rows, filters, filter],
  )
  const [selectedValue, setSelectedValue] = useState("")
  const filtered = useMemo(() => {
    const q = query.trim().toLocaleLowerCase()
    const result = rows.filter(
      (r) =>
        searchText(r).toLocaleLowerCase().includes(q) &&
        (!filter ||
          !choices.length ||
          !selectedValue ||
          filters.find((f) => f.key === filter)?.value(r) === selectedValue),
    )
    const col = columns.find((c) => c.key === sort)
    if (col?.sortValue)
      result.sort((a, b) => {
        const x = col.sortValue!(a),
          y = col.sortValue!(b)
        return (
          (typeof x === "number" && typeof y === "number"
            ? x - y
            : String(x).localeCompare(String(y))) * (direction === "asc" ? 1 : -1)
        )
      })
    return result
  }, [rows, query, sort, direction, columns, searchText, filter, filters, choices, selectedValue])
  useEffect(() => {
    setSelectedValue("")
    setPage(0)
  }, [filter])
  const server = serverPage !== undefined
  const pages = Math.max(1, Math.ceil(filtered.length / pageSize))
  const current = Math.min(page, pages - 1)
  const visible = server ? rows : filtered.slice(current * pageSize, (current + 1) * pageSize)
  const reset = () => {
    setQuery("")
    setSort("")
    setDirection("asc")
    setPage(0)
    setFilter("")
    setSelectedValue("")
  }
  return (
    <section className="adm-table-card">
      {!server && (
        <div className="adm-table-tools">
          <label className="adm-search">
            <Search size={17} />
            <input
              aria-label="Search records"
              placeholder="Search by name, ID, or keyword..."
              value={query}
              onChange={(e) => {
                setQuery(e.target.value)
                setPage(0)
              }}
            />
          </label>
          <div className="adm-table-controls">
            {filters.length > 0 && (
              <>
                <ListFilter size={16} />
                <select
                  aria-label="Filter field"
                  value={filter}
                  onChange={(e) => setFilter(e.target.value)}
                >
                  <option value="">All categories</option>
                  {filters.map((f) => (
                    <option key={f.key} value={f.key}>
                      {f.label}
                    </option>
                  ))}
                </select>
                {filter && (
                  <select
                    aria-label="Filter value"
                    value={selectedValue}
                    onChange={(e) => {
                      setSelectedValue(e.target.value)
                      setPage(0)
                    }}
                  >
                    <option value="">All values</option>
                    {choices.map((c) => (
                      <option key={c}>{c}</option>
                    ))}
                  </select>
                )}
              </>
            )}
            <ArrowDownUp size={15} />
            <select
              aria-label="Sort by"
              value={sort}
              onChange={(e) => {
                setSort(e.target.value)
                setPage(0)
              }}
            >
              <option value="">Default order</option>
              {columns
                .filter((c) => c.sortValue)
                .map((c) => (
                  <option key={c.key} value={c.key}>
                    {c.label}
                  </option>
                ))}
            </select>
            {sort && (
              <button
                className="adm-btn adm-btn-quiet"
                onClick={() => setDirection((v) => (v === "asc" ? "desc" : "asc"))}
              >
                {direction === "asc" ? "↑ Asc" : "↓ Desc"}
              </button>
            )}
            <button className="adm-btn adm-btn-quiet" onClick={reset}>
              <RotateCcw size={15} /> Reset
            </button>
          </div>
        </div>
      )}
      <div className="adm-table-scroll">
        <table className="adm-table">
          <thead>
            <tr>
              {columns.map((c) => (
                <th key={c.key}>{c.label}</th>
              ))}
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {visible.map((r) => (
              <tr
                key={r.id}
                onClick={() => onOpen(r)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault()
                    onOpen(r)
                  }
                }}
                tabIndex={0}
                aria-label={`Open record ${r.id}`}
              >
                {columns.map((c) => (
                  <td key={c.key}>{c.render(r)}</td>
                ))}
                <td>
                  <button
                    className="adm-btn adm-btn-outline"
                    onClick={(e) => {
                      e.stopPropagation()
                      onOpen(r)
                    }}
                  >
                    View details
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {!visible.length && (
          <div className="adm-empty">
            <strong>No matching records</strong>
            <span>{empty}</span>
            <button className="adm-btn" onClick={reset}>
              Clear filters
            </button>
          </div>
        )}
      </div>
      <div className="adm-table-footer">
        <span>
          {server
            ? `Page ${serverPage + 1} · ${serverTotal ?? filtered.length} total records`
            : `${filtered.length} records · Page ${current + 1} of ${pages}`}
        </span>
        <div className="adm-pagination">
          <button
            aria-label="Previous page"
            className="adm-icon-button"
            disabled={server ? serverPage === 0 : page === 0}
            onClick={() =>
              server ? onServerPage?.(serverPage - 1) : setPage((v) => Math.max(0, v - 1))
            }
          >
            <ChevronLeft size={17} />
          </button>
          <span className="adm-page-number">{server ? serverPage + 1 : current + 1}</span>
          <button
            aria-label="Next page"
            className="adm-icon-button"
            disabled={server ? !serverNext : page >= pages - 1}
            onClick={() =>
              server ? onServerPage?.(serverPage + 1) : setPage((v) => Math.min(pages - 1, v + 1))
            }
          >
            <ChevronRight size={17} />
          </button>
        </div>
      </div>
    </section>
  )
}
