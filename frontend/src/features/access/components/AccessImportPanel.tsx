import { useEffect, useState } from "react";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { accessImportApi } from "../api/accessImportApi";
import type { ImportRow, ImportResult } from "../types/accessImport.types";
import type { ProductType } from "../types/access.types";
import { useAction } from "@/shared/hooks/useRemote";
import { ActionNotice } from "@/shared/ui/RemoteState";
import { parseCsv } from "@/shared/utils/parseCsv";
import { exportCsv } from "@/shared/utils/exportCsv";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
export function AccessImportPanel({
  org,
  type,
  productId,
  expiresAt,
  changed,
}: {
  org: string;
  type: ProductType;
  productId: string;
  expiresAt: string | null;
  changed: () => void;
}) {
  const action = useAction();
  const { setDirty } = useOrganizationContext();
  const [text, setText] = useState(""),
    [cohort, setCohort] = useState(""),
    [plan, setPlan] = useState<ImportRow[] | null>(null),
    [results, setResults] = useState<ImportResult[] | null>(null),
    [csvError, setCsvError] = useState("");
  useEffect(() => {
    setPlan(null);
    setResults(null);
  }, [text, cohort, org, type, productId, expiresAt]);
  useEffect(() => {
    const key = `access-import-${org}`;
    setDirty(key, Boolean(text || cohort) && results === null);
    return () => setDirty(key, false);
  }, [org, text, cohort, results, setDirty]);
  const entries = text
    .split(/\r?\n/)
    .map((s) => s.trim())
    .filter(Boolean);
  const input = { entries, cohort, type, productId, expiresAt };
  return (
    <section className="sporg-card">
      <h2>Employee / candidate access import</h2>
      <p>
        Select a product above. Paste one account UUID or email per line, or
        import CSV with an identity / learnerId / email header. Preview
        validates each row; apply revalidates on the server and reports partial
        failures. Existing active grants are reused.
      </p>
      <Button
        variant="outline"
        onClick={() =>
          exportCsv("access-import-template.csv", [
            { identity: "learner@example.com" },
          ])
        }
      >
        Download CSV template
      </Button>
      <form
        className="sporg-form"
        onSubmit={(e) => {
          e.preventDefault();
          setResults(null);
          void action.run(async () => {
            setPlan(await accessImportApi.preview(org, input));
          }, "Validation finished; review all rows before granting.");
        }}
      >
        <Input
          label="Employee / candidate group label"
          maxLength={120}
          value={cohort}
          onChange={(e) => setCohort(e.target.value)}
        />
        <label>
          CSV (up to 1,000 rows / 1 MB)
          <input
            type="file"
            accept=".csv,text/csv"
            disabled={action.busy}
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (!file) return;
              void file
                .text()
                .then((data) => {
                  if (file.size > 1048576)
                    throw new Error("CSV must be at most 1 MB");
                  const rows = parseCsv(data);
                  const headers =
                    rows[0]?.map((c) => c.trim().toLowerCase()) ?? [];
                  let column = headers.findIndex((h) =>
                    ["identity", "learnerid", "email"].includes(h),
                  );
                  const body = column < 0 ? rows : rows.slice(1);
                  if (column < 0) column = 0;
                  if (body.length > 1000)
                    throw new Error("Import at most 1,000 rows");
                  setText(body.map((r) => r[column] ?? "").join("\n"));
                  setCsvError("");
                })
                .catch((e) => setCsvError(e.message));
              e.target.value = "";
            }}
          />
        </label>
        <label>
          UUIDs / emails
          <textarea
            required
            rows={7}
            value={text}
            onChange={(e) => setText(e.target.value)}
          />
        </label>
        {csvError && <p role="alert">{csvError}</p>}
        <Button
          type="submit"
          disabled={
            action.busy ||
            !productId ||
            entries.length === 0 ||
            entries.length > 1000
          }
        >
          Validate import
        </Button>
      </form>
      <ActionNotice {...action} />
      {plan && (
        <>
          <h3>
            {plan.filter((r) => r.eligible).length} eligible / {plan.length}{" "}
            rows
          </h3>
          <div className="sporg-table-scroll">
            <table>
              <thead>
                <tr>
                  <th>Row</th>
                  <th>Identity</th>
                  <th>Result</th>
                </tr>
              </thead>
              <tbody>
                {plan.map((r) => (
                  <tr key={r.row}>
                    <td>{r.row}</td>
                    <td>{r.identity}</td>
                    <td>{r.eligible ? "Eligible" : r.error}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Button
            disabled={action.busy || !plan.some((r) => r.eligible)}
            onClick={() => {
              if (
                window.confirm(
                  `Grant ${type.toLowerCase()} to ${plan.filter((r) => r.eligible).length} eligible learners?`,
                )
              )
                void action.run(async () => {
                  setResults(await accessImportApi.apply(org, input));
                  changed();
                }, "Import finished. Inspect the per-row results.");
            }}
          >
            Grant eligible rows
          </Button>
        </>
      )}
      {results && (
        <>
          <h3>Applied results</h3>
          {results.map((r) => (
            <p key={r.row}>
              {r.row}. {r.identity} ·{" "}
              {r.success ? `Granted (${r.grant?.id})` : r.error}
            </p>
          ))}
          <Button
            variant="outline"
            onClick={() =>
              exportCsv(
                "access-import-results.csv",
                results.map((r) => ({
                  row: r.row,
                  identity: r.identity,
                  success: r.success,
                  grantId: r.grant?.id,
                  error: r.error,
                })),
              )
            }
          >
            Export results
          </Button>
        </>
      )}
    </section>
  );
}
