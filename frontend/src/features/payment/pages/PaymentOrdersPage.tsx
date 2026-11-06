import { useCallback, useState } from "react"
import { Link, useSearchParams } from "react-router-dom"
import LearnerShell from "@/shared/components/layout/LearnerShell"
import { useRemote } from "@/shared/hooks/useRemote"
import { RemoteState } from "@/shared/ui/RemoteState"
import Pagination from "@/shared/ui/Pagination"
import Button from "@/shared/ui/Button"
import { paymentApi } from "../api/paymentApi"
export default function PaymentOrdersPage() {
  const [status, setStatus] = useState("")
  const [page, setPage] = useState(0)
  const [params] = useSearchParams()
  const returnedOrder = params.get("vnp_TxnRef") ?? params.get("orderId") ?? ""
  const remote = useRemote(useCallback(() => paymentApi.search(status, page), [status, page]))
  return (
    <LearnerShell>
      <section className="sporg">
        <header className="sporg-heading">
          <h1>Payment orders</h1>
          <p>
            Only verified provider notifications settle payments. A browser return does not grant
            access. Purchasing access and enrolling are separate actions.
          </p>
        </header>
        {returnedOrder && <ReturnedOrder id={returnedOrder} />}
        <label>
          Order status
          <select
            value={status}
            onChange={(e) => {
              setStatus(e.target.value)
              setPage(0)
            }}
          >
            <option value="">All statuses</option>
            {["PENDING", "PAID", "FAILED", "CANCELLED", "EXPIRED"].map((s) => (
              <option key={s}>{s}</option>
            ))}
          </select>
        </label>
        <RemoteState {...remote} retry={remote.reload} />
        <Button variant="outline" onClick={remote.reload}>
          Refresh orders
        </Button>
        {remote.data?.content.length === 0 && <p>No orders match this filter.</p>}
        {remote.data?.content.map((order) => (
          <article key={order.id} className="sporg-card">
            <h2>{order.status}</h2>
            <p>
              {order.amountVnd.toLocaleString("vi-VN")} VND · {order.productType}
            </p>
            <p>
              Created {new Date(order.createdAt).toLocaleString()} · Expires{" "}
              {new Date(order.expiresAt).toLocaleString()}
            </p>
            <code>{order.id}</code>
            {["PENDING", "EXPIRED", "FAILED", "CANCELLED"].includes(order.status) && (
              <p>
                <Link
                  to={`/checkout?type=${encodeURIComponent(order.productType)}&productId=${encodeURIComponent(order.productId)}`}
                >
                  Open checkout for this product
                </Link>
              </p>
            )}
          </article>
        ))}
        {remote.data && (
          <Pagination {...remote.data} page={page} busy={remote.loading} onChange={setPage} />
        )}
        <Link to="/catalog">Return to course catalog</Link>
      </section>
    </LearnerShell>
  )
}
function ReturnedOrder({ id }: { id: string }) {
  const remote = useRemote(useCallback(() => paymentApi.detail(id), [id]))
  return (
    <section className="sporg-card">
      <h2>Returned payment</h2>
      <p>
        The status below comes from the backend. Provider notification may arrive after your browser
        returns.
      </p>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data && (
        <p role="status">
          {remote.data.status} · {remote.data.amountVnd.toLocaleString("vi-VN")} VND
        </p>
      )}
      <Button onClick={remote.reload} variant="outline">
        Check returned order
      </Button>
    </section>
  )
}
