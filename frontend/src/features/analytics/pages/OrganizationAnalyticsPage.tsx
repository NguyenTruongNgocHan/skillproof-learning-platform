import { workspaceCourseApi } from "@/features/course/api/workspaceCourseApi";
import { useCallback, useState } from "react";
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { analyticsApi } from "../api/analyticsApi";
import { useRemote } from "@/shared/hooks/useRemote";
import { RemoteState } from "@/shared/ui/RemoteState";
import { exportCsv } from "@/shared/utils/exportCsv";
import Input from "@/shared/ui/Input";
import Button from "@/shared/ui/Button";
export default function OrganizationAnalyticsPage() {
  const { organization } = useOrganizationContext();
  const org = organization?.id ?? "";
  const directory = useRemote(
    useCallback(() => workspaceCourseApi.directory(org), [org]),
  );
  const [dates, setDates] = useState({
    version: "",
    campaign: "",
    start: new Date(Date.now() - 30 * 86400000).toISOString().slice(0, 10),
    end: new Date().toISOString().slice(0, 10),
  });
  const [range, setRange] = useState(dates);
  const remote = useRemote(
    useCallback(
      () =>
        analyticsApi.dashboard(
          org,
          new Date(range.start + "T00:00:00Z").toISOString(),
          new Date(
            new Date(range.end + "T00:00:00Z").getTime() + 86400000,
          ).toISOString(),
          range.version,
          range.campaign,
        ),
      [org, range],
    ),
  );
  return (
    <OrganizerSurface
      title="Reach & learning analytics"
      description="Compare traffic, campaigns and verified server conversions. Dates use UTC; select up to 93 days."
      authority="VIEW_ANALYTICS"
    >
      <form
        className="sporg-toolbar"
        onSubmit={(e) => {
          e.preventDefault();
          setRange({ ...dates });
        }}
      >
        <Input
          label="From"
          type="date"
          required
          value={dates.start}
          onChange={(e) => setDates({ ...dates, start: e.target.value })}
        />
        <Input
          label="Through"
          type="date"
          required
          min={dates.start}
          value={dates.end}
          onChange={(e) => setDates({ ...dates, end: e.target.value })}
        />
        <label>
          Course version
          <select
            value={dates.version}
            onChange={(e) => setDates({ ...dates, version: e.target.value })}
          >
            <option value="">All course versions</option>
            {directory.data?.map((v) => (
              <option key={v.id} value={v.id}>
                {v.title} · {v.id}
              </option>
            ))}
          </select>
        </label>
        <Input
          label="UTM campaign (optional)"
          maxLength={120}
          value={dates.campaign}
          onChange={(e) => setDates({ ...dates, campaign: e.target.value })}
        />
        <Button type="submit">Apply range</Button>
        <Button
          variant="outline"
          disabled={!remote.data}
          onClick={() =>
            exportCsv(
              "analytics-daily.csv",
              remote.data!.daily.map((r) => ({ ...r })),
            )
          }
        >
          Export daily data
        </Button>
      </form>
      <RemoteState {...directory} retry={directory.reload} />
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data && (
        <>
          <section className="sporg-card">
            <h2>Authenticated learning funnel</h2>
            <p>{remote.data.funnel.basis}</p>
            <div className="sporg-metrics">
              {(
                [
                  ["Viewed", remote.data.funnel.detailViews],
                  ["Enrolled", remote.data.funnel.enrollments],
                  ["Started learning", remote.data.funnel.learning],
                  ["Completed", remote.data.funnel.completions],
                ] as const
              ).map(([label, count]) => (
                <div key={label}>
                  <small>{label}</small>
                  <strong>{count}</strong>
                  <progress
                    aria-label={label}
                    value={count}
                    max={Math.max(1, remote.data!.funnel.detailViews)}
                  />
                </div>
              ))}
            </div>
            <p>
              {remote.data.funnel.checkouts} cohorts reached checkout.
              Free/granted enrollment can bypass checkout. Counts group each
              account and course version, using events in chronological order.
            </p>
          </section>
          <section className="sporg-card">
            <h2>Gross captured payments</h2>
            <p>
              {remote.data.revenue.grossPaidVnd.toLocaleString("vi-VN")} VND ·{" "}
              {remote.data.revenue.paidOrders} paid orders
            </p>
            <small>
              Organization-wide verified paid amounts in this period;
              course/campaign filters apply to traffic and funnel only. Platform
              fees, refunds, payouts and settlement are outside this report.
            </small>
          </section>
          <div className="sporg-metrics">
            {remote.data.metrics.map((m) => (
              <section className="sporg-card" key={m.kind}>
                <small>{m.kind.toLowerCase().replaceAll("_", " ")}</small>
                <strong>{m.events.toLocaleString()}</strong>
                <p>
                  {m.visitors} distinct visitor identifiers ·{" "}
                  {m.authenticatedAccounts} authenticated accounts
                </p>
              </section>
            ))}
          </div>
          {remote.data.metrics.length === 0 && (
            <section className="sporg-empty">
              <h2>No events in this period</h2>
              <p>
                No historical traffic is invented. Telemetry starts when
                tracking is deployed.
              </p>
            </section>
          )}
          <section className="sporg-card">
            <h2>Daily activity</h2>
            {remote.data.daily.map((r, i) => (
              <div className="sporg-trend-row" key={i}>
                <span>
                  {r.date} · {r.kind}
                </span>
                <progress
                  aria-label={`${r.date} ${r.kind}`}
                  value={r.events}
                  max={Math.max(1, ...remote.data!.daily.map((d) => d.events))}
                />
                <strong>{r.events}</strong>
              </div>
            ))}
          </section>
          <section className="sporg-card">
            <h2>Campaign & source comparison</h2>
            <div className="sporg-table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Source</th>
                    <th>Campaign</th>
                    <th>Event</th>
                    <th>Count</th>
                  </tr>
                </thead>
                <tbody>
                  {remote.data.sources.map((r, i) => (
                    <tr key={i}>
                      <td>{r.source}</td>
                      <td>{r.campaign || "—"}</td>
                      <td>{r.kind}</td>
                      <td>{r.events}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
          <section className="sporg-card">
            <h2>Course versions</h2>
            {remote.data.courses.map((r, i) => (
              <div className="sporg-row" key={i}>
                <span>
                  {r.title}
                  <small>{r.versionId}</small>
                </span>
                <span>
                  {r.kind}: {r.events}
                </span>
              </div>
            ))}
          </section>
          <p>
            Anonymous visitor counts are approximate. Client views are not
            evidence of learning; enrollment/payment/completion events are
            recorded by backend transactions.
          </p>
        </>
      )}
    </OrganizerSurface>
  );
}
