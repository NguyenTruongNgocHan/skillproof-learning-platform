import Button from "./Button"
export default function Pagination({
  page,
  totalPages,
  totalElements,
  busy,
  onChange,
}: {
  page: number
  totalPages: number
  totalElements: number
  busy?: boolean
  onChange: (page: number) => void
}) {
  return (
    <nav className="platform-pagination" aria-label="Pagination">
      <span>
        {totalElements} records · Page {page + 1} of {Math.max(1, totalPages)}
      </span>
      <div>
        <Button variant="outline" disabled={busy || page === 0} onClick={() => onChange(page - 1)}>
          Previous page
        </Button>
        <Button
          variant="outline"
          disabled={busy || page + 1 >= totalPages}
          onClick={() => onChange(page + 1)}
        >
          Next page
        </Button>
      </div>
    </nav>
  )
}
