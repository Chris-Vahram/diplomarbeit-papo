package at.papo.partnerportal.shop;

// WCAG 2.x contrast for #RRGGBB colours, mirrored from frontend/src/shop/contrast.js. Both run
// the shared vectors in tools/fixtures/shop-contrast-vectors.json (ShopContrastTest).
final class ShopContrast {

    private ShopContrast() {
    }

    static double luminance(String hex) {
        double[] rgb = new double[3];
        for (int i = 0; i < 3; i++) {
            double value = Integer.parseInt(hex.substring(1 + 2 * i, 3 + 2 * i), 16) / 255.0;
            rgb[i] = value <= 0.03928 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
        }
        return 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2];
    }

    static double contrast(String a, String b) {
        double la = luminance(a);
        double lb = luminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    // Text colour the renderer puts on a filled background (shop-theme.js applyColors).
    static String onColor(String hex) {
        return contrast("#111111", hex) > contrast("#ffffff", hex) ? "#111111" : "#ffffff";
    }

    // color-mix(in srgb, a share, b), as shop.css derives --shop-muted.
    static String mix(String a, String b, double share) {
        StringBuilder out = new StringBuilder("#");
        for (int i = 0; i < 3; i++) {
            int x = Integer.parseInt(a.substring(1 + 2 * i, 3 + 2 * i), 16);
            int y = Integer.parseInt(b.substring(1 + 2 * i, 3 + 2 * i), 16);
            out.append(String.format("%02x", Math.round(x * share + y * (1 - share))));
        }
        return out.toString();
    }
}
