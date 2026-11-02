package at.papo.partnerportal.shop;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

// Reduces request data to what the statistics may keep (shop-statistics "Anonymous event
// collection"): a referrer URL to its domain, a user-agent to a device class. The full values
// are only read here, in memory, and never stored or logged.
final class ShopStatReduction {

    static final String MOBILE = "Mobil";
    static final String TABLET = "Tablet";
    static final String DESKTOP = "Desktop";

    private static final Pattern HOST = Pattern.compile("^[a-z0-9]([a-z0-9-]*[a-z0-9])?(\\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)+$");
    private static final Pattern IPV4 = Pattern.compile("^[0-9.]+$");
    private static final Pattern BOT = Pattern.compile(
            "bot|crawl|spider|slurp|scrape|fetch|preview|headless|lighthouse|pingdom|monitor|curl|wget|"
                    + "python|java/|okhttp|go-http|httpclient|axios|node-fetch|facebookexternalhit|embedly",
            Pattern.CASE_INSENSITIVE);

    private ShopStatReduction() {
    }

    // "https://www.example.com/search?q=lampe" -> "example.com". Empty for direct visits, invalid
    // URLs, IP literals and the shop's own host, so only a plain domain name is ever counted.
    static String referrerDomain(String referrer, String ownHost) {
        if (referrer == null || referrer.isBlank() || referrer.length() > 2048) {
            return "";
        }
        String host;
        try {
            URI uri = new URI(referrer.strip());
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https") || uri.getHost() == null) {
                return "";
            }
            host = uri.getHost().toLowerCase(Locale.ROOT);
        } catch (Exception invalid) {
            return "";
        }
        if (host.startsWith("www.")) {
            host = host.substring(4);
        }
        if (host.length() > ShopStatDaily.DIMENSION_MAX || IPV4.matcher(host).matches() || !HOST.matcher(host).matches()) {
            return "";
        }
        String own = ownHost == null ? "" : ownHost.toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "").replaceFirst(":\\d+$", "");
        return host.equals(own) ? "" : host;
    }

    static boolean isBot(String userAgent) {
        return userAgent == null || userAgent.isBlank() || BOT.matcher(userAgent).find();
    }

    static String deviceClass(String userAgent) {
        String agent = userAgent == null ? "" : userAgent;
        if (agent.contains("iPad") || agent.contains("Tablet") || agent.contains("Android") && !agent.contains("Mobile")) {
            return TABLET;
        }
        if (agent.contains("Mobi") || agent.contains("iPhone") || agent.contains("Android")) {
            return MOBILE;
        }
        return DESKTOP;
    }
}
