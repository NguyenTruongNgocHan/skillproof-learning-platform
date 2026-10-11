import { useCallback, useEffect, useState } from "react";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { pricingApi } from "../api/pricingApi";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Input from "@/shared/ui/Input";
import Button from "@/shared/ui/Button";
const money = (n: number) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(
    n,
  );
export function CourseCommercialPanel({
  versionId,
  onDirtyChange,
}: {
  versionId: string;
  onDirtyChange?: (dirty: boolean) => void;
}) {
  const { grants, setDirty, confirmDiscard } = useOrganizationContext();
  const remote = useRemote(
    useCallback(() => pricingApi.get(versionId), [versionId]),
  );
  const action = useAction(remote.reload);
  const [form, setForm] = useState({
    accessMode: "PUBLIC",
    priceVnd: 0,
    certificationPriceVnd: 0,
    reason: "",
  });
  const [promotion, setPromotion] = useState({
    name: "",
    percent: 10,
    startsAt: "",
    endsAt: "",
  });
  const [dirty, markDirty] = useState(false);
  useEffect(() => {
    setDirty("commercial", dirty);
    onDirtyChange?.(dirty);
    return () => {
      setDirty("commercial", false);
      onDirtyChange?.(false);
    };
  }, [dirty, setDirty, onDirtyChange]);
  useEffect(() => {
    if (remote.data) {
      setForm({
        accessMode: remote.data.baseOffer.accessMode,
        priceVnd: remote.data.baseOffer.learningPrice,
        certificationPriceVnd: remote.data.baseOffer.certificationPrice,
        reason: "",
      });
      markDirty(false);
    }
  }, [remote.data]);
  const canPrice = grants.includes("MANAGE_PRICING");
  return (
    <>
      <RemoteState {...remote} retry={remote.reload} />
      <ActionNotice {...action} />
      {remote.data && (
        <>
          <section className="sporg-card">
            <h2>Current offer & availability</h2>
            <p>
              {remote.data.offer.state} · {remote.data.offer.accessMode} ·
              Learning {money(remote.data.offer.learningPrice)} · Certification{" "}
              {money(remote.data.offer.certificationPrice)}
            </p>
            <p>
              These are effective prices including a running promotion. Price
              and promotion changes are independent of immutable course content.
              Existing orders retain their checkout price.
            </p>
            <form
              className="sporg-form"
              onSubmit={(e) => {
                e.preventDefault();
                if (window.confirm("Replace base prices now?"))
                  void action.run(
                    () => pricingApi.save(versionId, form),
                    "Price change recorded.",
                  );
              }}
            >
              <fieldset disabled={!canPrice || action.busy}>
                <label>
                  Access mode
                  <select
                    value={form.accessMode}
                    onChange={(e) => {
                      setForm({
                        ...form,
                        accessMode: e.target.value,
                        priceVnd:
                          e.target.value === "RESTRICTED" ? 0 : form.priceVnd,
                        certificationPriceVnd:
                          e.target.value === "RESTRICTED"
                            ? 0
                            : form.certificationPriceVnd,
                      });
                      markDirty(true);
                    }}
                  >
                    <option>PUBLIC</option>
                    <option>RESTRICTED</option>
                  </select>
                </label>
                <div className="sporg-fields">
                  <Input
                    label="New learning base price (VND)"
                    required
                    type="number"
                    min={0}
                    max={1000000000}
                    value={form.priceVnd}
                    onChange={(e) => {
                      setForm({ ...form, priceVnd: Number(e.target.value) });
                      markDirty(true);
                    }}
                  />
                  <Input
                    label="New certification base price (VND)"
                    required
                    type="number"
                    min={0}
                    max={1000000000}
                    value={form.certificationPriceVnd}
                    onChange={(e) => {
                      setForm({
                        ...form,
                        certificationPriceVnd: Number(e.target.value),
                      });
                      markDirty(true);
                    }}
                  />
                </div>
                <Input
                  label="Reason"
                  required
                  maxLength={1000}
                  value={form.reason}
                  onChange={(e) => {
                    setForm({ ...form, reason: e.target.value });
                    markDirty(true);
                  }}
                />
                <Button type="submit" disabled={!dirty}>
                  Save new base prices
                </Button>
              </fieldset>
            </form>
            <div className="sporg-actions">
              {["ACTIVE", "CLOSED", "ARCHIVED"].map((state) => (
                <Button
                  variant="outline"
                  key={state}
                  disabled={
                    action.busy ||
                    state === remote.data?.offer.state ||
                    !grants.includes("MANAGE_CONTENT")
                  }
                  onClick={() => {
                    if (
                      confirmDiscard("Discard unsaved commercial changes?") &&
                      window.confirm(
                        `${state}: existing enrollments and orders are retained. Confirm?`,
                      )
                    )
                      void action.run(
                        () => pricingApi.lifecycle(versionId, state),
                        "Availability updated.",
                      );
                  }}
                >
                  {state === "ACTIVE"
                    ? "Accept enrollments"
                    : state === "CLOSED"
                      ? "Close enrollment"
                      : "Archive course"}
                </Button>
              ))}
            </div>
          </section>
          <section className="sporg-card">
            <h2>Promotions</h2>
            {remote.data.promotions.map((p) => (
              <div className="sporg-row" key={p.id}>
                <span>
                  {p.name} · {p.percent}% ·{" "}
                  {new Date(p.startsAt).toLocaleString()} —{" "}
                  {new Date(p.endsAt).toLocaleString()} ·{" "}
                  {p.active ? "scheduled/active" : "cancelled"}
                </span>
                {p.active && canPrice && (
                  <Button
                    disabled={action.busy}
                    variant="outline"
                    onClick={() => {
                      if (
                        confirmDiscard(
                          "Discard unsaved commercial changes and cancel promotion?",
                        )
                      )
                        void action.run(
                          () => pricingApi.cancel(versionId, p.id),
                          "Promotion cancelled.",
                        );
                    }}
                  >
                    Cancel
                  </Button>
                )}
              </div>
            ))}
            {canPrice && (
              <form
                className="sporg-form"
                onSubmit={(e) => {
                  e.preventDefault();
                  if (
                    dirty &&
                    !window.confirm(
                      "Discard unsaved base prices and schedule this promotion?",
                    )
                  )
                    return;
                  void action.run(async () => {
                    await pricingApi.promote(versionId, {
                      ...promotion,
                      startsAt: new Date(promotion.startsAt).toISOString(),
                      endsAt: new Date(promotion.endsAt).toISOString(),
                    });
                    setPromotion({ ...promotion, name: "" });
                  }, "Promotion scheduled.");
                }}
              >
                <Input
                  label="Promotion name"
                  required
                  maxLength={180}
                  value={promotion.name}
                  onChange={(e) =>
                    setPromotion({ ...promotion, name: e.target.value })
                  }
                />
                <div className="sporg-fields">
                  <Input
                    label="Discount (%)"
                    type="number"
                    min={1}
                    max={90}
                    required
                    value={promotion.percent}
                    onChange={(e) =>
                      setPromotion({
                        ...promotion,
                        percent: Number(e.target.value),
                      })
                    }
                  />
                  <Input
                    label="Starts"
                    type="datetime-local"
                    required
                    value={promotion.startsAt}
                    onChange={(e) =>
                      setPromotion({ ...promotion, startsAt: e.target.value })
                    }
                  />
                  <Input
                    label="Ends"
                    type="datetime-local"
                    required
                    value={promotion.endsAt}
                    onChange={(e) =>
                      setPromotion({ ...promotion, endsAt: e.target.value })
                    }
                  />
                </div>
                <Button type="submit" disabled={action.busy}>
                  Schedule promotion
                </Button>
              </form>
            )}
          </section>
          <section className="sporg-card">
            <h2>Price history</h2>
            {remote.data.history.length === 0 && (
              <p>No price changes recorded yet.</p>
            )}
            {remote.data.history.map((h) => (
              <div className="sporg-evidence-row" key={h.id}>
                <strong>
                  {money(h.previousPrice)} → {money(h.priceVnd)} · Certification{" "}
                  {money(h.previousCertificationPrice)} →{" "}
                  {money(h.certificationPriceVnd)}
                </strong>
                <p>{h.reason}</p>
                <small>
                  {h.actorId} · {new Date(h.createdAt).toLocaleString()}
                </small>
              </div>
            ))}
          </section>
        </>
      )}
    </>
  );
}
