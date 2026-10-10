import Button from "./Button";
export function RemoteState({
  loading,
  error,
  retry,
}: {
  loading?: boolean;
  error?: string;
  retry?: () => void;
}) {
  if (error)
    return (
      <div className="sporg-alert" role="alert">
        <p>{error}</p>
        {retry && (
          <Button variant="outline" onClick={retry}>
            Try again
          </Button>
        )}
      </div>
    );
  if (loading)
    return (
      <div className="sporg-card" role="status" aria-busy="true">
        <p>Loading…</p>
        <div className="sporg-skeleton" />
        <div className="sporg-skeleton sporg-skeleton-short" />
      </div>
    );
  return null;
}
export function ActionNotice({
  error,
  success,
}: {
  error: string;
  success: string;
}) {
  return (
    <>
      {error && (
        <p className="sporg-alert" role="alert">
          {error}
        </p>
      )}
      {success && (
        <p className="sporg-success" role="status">
          {success}
        </p>
      )}
    </>
  );
}
