package at.papo.partnerportal.product;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArticleTaxTest {

    private static BigDecimal d(String value) {
        return new BigDecimal(value);
    }

    @Test
    void derivesNetAndTaxAtTwentyPercent() {
        ArticleTax.Totals totals = ArticleTax.totals(List.of(new ArticleTax.Line(d("12.00"), 1, d("20"))));
        assertEquals(d("12.00"), totals.gross());
        assertEquals(d("10.00"), totals.net());
        assertEquals(d("2.00"), totals.tax());
        assertEquals(d("10.00"), ArticleTax.net(d("12.00"), d("20")));
    }

    @Test
    void zeroRateKeepsGrossAsNet() {
        assertEquals(d("9.99"), ArticleTax.net(d("9.99"), d("0")));
        ArticleTax.Totals totals = ArticleTax.totals(List.of(new ArticleTax.Line(d("9.99"), 3, d("0.00"))));
        assertEquals(d("29.97"), totals.net());
        assertEquals(d("0.00"), totals.tax());
    }

    @Test
    void roundsHalfUpAtTheCentBoundary() {
        // 0.01 / 1.2 = 0.00833.. -> 0.01; 0.03 / 1.2 = 0.025 exactly -> 0.03 (half up, not half even)
        assertEquals(d("0.01"), ArticleTax.net(d("0.01"), d("20")));
        assertEquals(d("0.03"), ArticleTax.net(d("0.03"), d("20")));
        // 0.11 / 1.1 = 0.1 exactly; 0.05 / 1.1 = 0.04545.. -> 0.05
        assertEquals(d("0.10"), ArticleTax.net(d("0.11"), d("10")));
        assertEquals(d("0.05"), ArticleTax.net(d("0.05"), d("10")));
    }

    @Test
    void totalsRoundOnceSoNetPlusTaxEqualsGross() {
        // Seven lines of 0.01 at 13 %: rounded per line 7 x 0.01 = 0.07, rounded once 0.0619.. -> 0.06.
        List<ArticleTax.Line> lines = java.util.stream.IntStream.range(0, 7)
                .mapToObj(i -> new ArticleTax.Line(d("0.01"), 1, d("13"))).toList();
        ArticleTax.Totals totals = ArticleTax.totals(lines);
        assertEquals(d("0.07"), totals.gross());
        assertEquals(d("0.06"), totals.net());
        assertEquals(d("0.01"), totals.tax());

        ArticleTax.Totals mixed = ArticleTax.totals(List.of(
                new ArticleTax.Line(d("89.90"), 3, d("20")),
                new ArticleTax.Line(d("14.50"), 7, d("13")),
                new ArticleTax.Line(d("6.90"), 1, d("0"))));
        assertEquals(mixed.gross(), mixed.net().add(mixed.tax()));
        assertEquals(d("378.10"), mixed.gross());
        assertEquals(d("321.47"), mixed.net());
    }

    @Test
    void convertsNetToGross() {
        assertEquals(d("12.00"), ArticleTax.grossFromNet(d("10.00"), d("20")));
        assertEquals(d("11.30"), ArticleTax.grossFromNet(d("10"), d("13")));
        assertEquals(d("0.01"), ArticleTax.grossFromNet(d("0.01"), d("0")));
    }

    @Test
    void acceptsOnlyTheFourRates() {
        assertTrue(ArticleTax.isAllowed(d("20.00")));
        assertTrue(ArticleTax.isAllowed(d("13")));
        assertTrue(ArticleTax.isAllowed(d("10")));
        assertTrue(ArticleTax.isAllowed(d("0")));
        assertFalse(ArticleTax.isAllowed(d("19")));
        assertFalse(ArticleTax.isAllowed(d("20.5")));
        assertFalse(ArticleTax.isAllowed(null));
    }
}
