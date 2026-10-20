package at.papo.partnerportal.product;

import at.papo.partnerportal.common.ApiException;

// Verifies uploaded product images by sniffing magic bytes rather than trusting the
// multipart-declared content type (attacker-controlled). SVG is deliberately not accepted:
// it can carry script and would be served same-origin from the API (see design.md).
public final class ProductImageValidator {

    static final int MAX_SIZE_BYTES = 2 * 1024 * 1024;

    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };
    private static final byte[] JPEG_SIGNATURE = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
    private static final byte[] RIFF_SIGNATURE = { 'R', 'I', 'F', 'F' };
    private static final byte[] WEBP_SIGNATURE = { 'W', 'E', 'B', 'P' };

    private ProductImageValidator() {
    }

    // Returns the verified content type, or throws an ApiException with a German
    // message if the file is too large or is not a recognised PNG/JPEG/WEBP image.
    static String detectContentType(byte[] data) {
        if (data.length == 0) {
            throw new ApiException(400, "Datei ist leer.");
        }
        if (data.length > MAX_SIZE_BYTES) {
            throw new ApiException(400, "Bild ist zu groß (maximal 2 MB erlaubt).");
        }
        String contentType = sniffContentType(data);
        if (contentType == null) {
            throw new ApiException(400,
                    "Ungültiges Bildformat. Erlaubt sind PNG, JPEG und WEBP (kein SVG oder andere Dateien).");
        }
        return contentType;
    }

    // PNG, JPEG or WEBP from the magic bytes, otherwise null. Shared with shop assets, which
    // have their own size limit and messages.
    public static String sniffContentType(byte[] data) {
        if (startsWith(data, PNG_SIGNATURE)) {
            return "image/png";
        }
        if (startsWith(data, JPEG_SIGNATURE)) {
            return "image/jpeg";
        }
        if (isWebp(data)) {
            return "image/webp";
        }
        return null;
    }

    private static boolean isWebp(byte[] data) {
        return data.length >= 12
                && startsWith(data, RIFF_SIGNATURE)
                && matchesAt(data, 8, WEBP_SIGNATURE);
    }

    private static boolean startsWith(byte[] data, byte[] signature) {
        return matchesAt(data, 0, signature);
    }

    private static boolean matchesAt(byte[] data, int offset, byte[] signature) {
        if (data.length < offset + signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (data[offset + i] != signature[i]) {
                return false;
            }
        }
        return true;
    }
}
