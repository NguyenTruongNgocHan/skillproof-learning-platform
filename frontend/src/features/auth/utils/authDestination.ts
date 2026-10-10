export function safeDestination(value: string | null): string | null {
  if (!value || !value.startsWith("/") || value.startsWith("//") || /[\\\r\n]/.test(value))
    return null
  const url = new URL(value, window.location.origin)
  return url.origin === window.location.origin ? url.pathname + url.search + url.hash : null
}
