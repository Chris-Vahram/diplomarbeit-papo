package at.papo.partnerportal.product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// USt rules of the article-tax spec: prices are stored gross, net and tax are derived.
// The four Austrian rates are a fixed set (design.md D1, no TaxRate entity).
public final class ArticleTax {

    public static final BigDecimal DEFAULT_RATE = BigDecimal.valueOf(20);

    public static final List<BigDecimal> ALLOWED_RATES = List.of(
            BigDecimal.valueOf(20), BigDecimal.valueOf(13), BigDecimal.valueOf(10), BigDecimal.ZERO);

    public static final String INVALID_RATE_MESSAGE = "Bitte einen gültigen Steuersatz wählen.";

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    // Intermediate scale for unrounded line values; far below a cent, rounded once per total.
    private static final int EXACT_SCALE = 10;

    private ArticleTax() {
    }

    public static boolean isAllowed(BigDecimal rate) {
        return rate != null && ALLOWED_RATES.stream().anyMatch(allowed -> allowed.compareTo(rate) == 0);
    }

    // Stored representation: same scale for equal rates, so 20 and 20.00 compare and print alike.
    public static BigDecimal normalized(BigDecimal rate) {
        return rate.setScale(2, RoundingMode.UNNECESSARY);
    }

    // Net unit price, rounded to cents (display).
    public static BigDecimal net(BigDecimal gross, BigDecimal rate) {
        return exactNet(gross, rate).setScale(2, RoundingMode.HALF_UP);
    }

    // Gross from a net source price (article import with a net price column).
    public static BigDecimal grossFromNet(BigDecimal net, BigDecimal rate) {
        return net.multiply(HUNDRED.add(rate)).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    static BigDecimal exactNet(BigDecimal gross, BigDecimal rate) {
        return gross.multiply(HUNDRED).divide(HUNDRED.add(rate), EXACT_SCALE, RoundingMode.HALF_UP);
    }

    public record Line(BigDecimal unitGross, int quantity, BigDecimal rate) {
    }

    // Gross is exact (cents x quantity); net is summed from unrounded line values and rounded
    // once; tax is the difference, so net + tax always equals gross (design.md D3).
    public record Totals(BigDecimal gross, BigDecimal net, BigDecimal tax) {
    }

    public static Totals totals(List<Line> lines) {
        BigDecimal gross = BigDecimal.ZERO.setScale(2);
        BigDecimal exactNet = BigDecimal.ZERO;
        for (Line line : lines) {
            BigDecimal lineGross = line.unitGross().multiply(BigDecimal.valueOf(line.quantity()));
            gross = gross.add(lineGross);
            exactNet = exactNet.add(exactNet(lineGross, line.rate()));
        }
        BigDecimal net = exactNet.setScale(2, RoundingMode.HALF_UP);
        return new Totals(gross, net, gross.subtract(net));
    }
}
