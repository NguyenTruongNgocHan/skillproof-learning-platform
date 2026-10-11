import { useCallback, useState, useRef } from "react";
import { useSearchParams, Link } from "react-router-dom";
import LearnerShell from "@/shared/components/layout/LearnerShell";
import { paymentApi } from "../api/paymentApi";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import type { Checkout } from "../types/payment.types";
export default function CheckoutPage() {
  const [params] = useSearchParams();
  const type = params.get("type") ?? "COURSE",
    product = params.get("productId") ?? "";
  const key = useRef(crypto.randomUUID());
  const remote = useRemote(useCallback(() => paymentApi.mine(), []));
  const action = useAction(remote.reload);
  const [checkout, setCheckout] = useState<Checkout | null>(null);
  const relevant =
    remote.data?.filter(
      (o) => o.productType === type && o.productId === product,
    ) ?? [];
  return (
    <LearnerShell>
      <main className="sporg">
        <header className="sporg-heading">
          <h1>Purchase access</h1>
          <p>
            Backend fixes the amount at checkout and grants access only after a
            verified payment notification. Certification access does not
            guarantee course completion or certificate issuance.
          </p>
        </header>
        <RemoteState {...remote} retry={remote.reload} />
        <ActionNotice {...action} />
        <section className="sporg-card">
          <p>
            Product: {type} · <code>{product}</code>
          </p>
          <Button
            disabled={action.busy || !product || !remote.data}
            onClick={() =>
              void action.run(async () => {
                const pending = relevant.find(
                  (o) =>
                    o.status === "PENDING" &&
                    new Date(o.expiresAt) > new Date(),
                );
                setCheckout(
                  await paymentApi.checkout(
                    type,
                    product,
                    pending?.idempotencyKey ?? key.current,
                  ),
                );
              }, "Checkout prepared. Review the amount before proceeding.")
            }
          >
            Prepare / resume checkout
          </Button>
          {checkout && (
            <>
              <h2>{checkout.amountVnd.toLocaleString("vi-VN")} VND</h2>
              <p>
                {checkout.status} · Expires{" "}
                {new Date(checkout.expiresAt).toLocaleString()}
              </p>
              {checkout.checkoutUrl && (
                <Button
                  onClick={() => {
                    const url = new URL(checkout.checkoutUrl!);
                    if (url.protocol !== "https:")
                      throw new Error("Secure payment URL required");
                    window.location.assign(url.href);
                  }}
                >
                  Continue to payment gateway
                </Button>
              )}
            </>
          )}
        </section>
        <section className="sporg-card">
          <h2>Payment status from backend</h2>
          <Button variant="outline" onClick={remote.reload}>
            Refresh status
          </Button>
          {relevant.map((o) => (
            <article className="sporg-evidence-row" key={o.id}>
              <strong>{o.status}</strong>
              <p>
                {o.amountVnd.toLocaleString("vi-VN")} VND ·{" "}
                {new Date(o.createdAt).toLocaleString()}
              </p>
              <code>{o.id}</code>
            </article>
          ))}
          <Link to="/catalog">Return to courses</Link>
        </section>
      </main>
    </LearnerShell>
  );
}
