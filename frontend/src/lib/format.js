// German (Austria) presentation formatting - the app is German-only, de-AT is
// hardcoded (design.md). Callers keep handling null/undefined (existing "–" fallbacks).

// For metric cards and inline text, e.g. "€ 391,80".
export function formatEuro(value) {
  return new Intl.NumberFormat("de-AT", { style: "currency", currency: "EUR" }).format(value);
}

// For table cells whose column header already carries "(€)", e.g. "38.940,20".
// de-DE (not de-AT) here: current ICU data groups plain (non-currency) de-AT
// numbers with a space, not a dot - de-DE reliably gives the dot-grouped,
// comma-decimal form the design/mockups actually show.
export function formatNumber(value) {
  return new Intl.NumberFormat("de-DE", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);
}

// Composes a structured Address (street/houseNumber/postalCode/city/country) back
// into one display string for read-only contexts (dropdowns, confirmations, details).
export function formatAddress(address) {
  if (!address) return "";
  return `${address.street} ${address.houseNumber}, ${address.postalCode} ${address.city}, ${address.country}`;
}

// ISO "YYYY-MM-DD" -> "dd.MM.yyyy" (zero-padded). Parses the parts manually and
// builds a local Date (not `new Date(isoString)`, which parses as UTC midnight
// and can shift the displayed calendar day in negative-UTC-offset timezones).
export function formatDate(isoString) {
  const [year, month, day] = isoString.split("-").map(Number);
  return new Intl.DateTimeFormat("de-AT", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(new Date(year, month - 1, day));
}
