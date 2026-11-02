// Integer-cent money arithmetic shared across the dashboard aggregation and the
// order-form live totals - float arithmetic on euro values accumulates rounding
// drift (project rule: money math in integer cents, never a naive float reduce).

export function toCents(euroValue) {
  return Math.round(euroValue * 100);
}

export function sumCents(values) {
  const cents = values.reduce((sum, v) => sum + toCents(v), 0);
  return cents / 100;
}

// Mirrors CommissionService.createForOrder exactly: BigDecimal orderValue
// .multiply(percent).divide(100, 2, HALF_UP). Computed as an exact integer
// division with an explicit half-up tie rule (not a float divide/Math.round,
// which can drift a cent right at the .5 boundary) so the preview equals the
// stored CommissionRecord to the cent.
export function commissionCentsHalfUp(orderValueCents, percent) {
  const percentBasisPoints = Math.round(percent * 100);
  const numerator = orderValueCents * percentBasisPoints;
  const denominator = 10000;
  const quotient = Math.floor(numerator / denominator);
  const remainder = numerator - quotient * denominator;
  return remainder * 2 >= denominator ? quotient + 1 : quotient;
}

// Mirrors ArticleTax.totals (backend): net = gross / (1 + rate/100) per line, summed
// unrounded and rounded half-up once, so the cart preview equals the stored net value
// to the cent. Exact rational sum in BigInt (rates are percent with up to two decimals);
// `lines` is [{ grossCents, ratePercent }] with grossCents already multiplied by quantity.
export function netCentsHalfUp(lines) {
  let numerator = 0n;
  let denominator = 1n;
  for (const { grossCents, ratePercent } of lines) {
    const lineDenominator = 10000n + BigInt(Math.round(Number(ratePercent ?? 20) * 100));
    // numerator/denominator + grossCents*10000/lineDenominator
    numerator = numerator * lineDenominator + BigInt(grossCents) * 10000n * denominator;
    denominator *= lineDenominator;
  }
  const quotient = numerator / denominator;
  const remainder = numerator - quotient * denominator;
  return Number(remainder * 2n >= denominator ? quotient + 1n : quotient);
}
