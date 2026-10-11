import { exportCsv } from "./exportCsv";
export async function exportPagedCsv<T>(
  name: string,
  load: (page: number) => Promise<{ content: T[]; totalPages: number }>,
  row: (
    value: T,
  ) => Record<string, string | number | boolean | null | undefined>,
) {
  const all: T[] = [];
  let page = 0,
    total = 1;
  do {
    const result = await load(page);
    total = result.totalPages;
    all.push(...result.content);
    if (all.length > 50000)
      throw new Error(
        "More than 50,000 rows; narrow your filters before exporting.",
      );
    page++;
  } while (page < total);
  exportCsv(name, all.map(row));
}
