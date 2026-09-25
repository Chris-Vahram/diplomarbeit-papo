package at.papo.partnerportal.partner;

import at.papo.partnerportal.common.ApiException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

final class PartnerTradeProofValidator {

    static final int MAX_SIZE_BYTES = 10 * 1024 * 1024;

    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };
    private static final byte[] JPEG_SIGNATURE = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };

    private PartnerTradeProofValidator() {
    }

    static String validate(byte[] data, String filename, String declaredContentType) {
        if (data == null || data.length == 0) {
            throw new ApiException(400, "Nachweisdatei ist leer.");
        }
        if (data.length > MAX_SIZE_BYTES) {
            throw new ApiException(413, "Nachweisdatei ist zu groß (maximal 10 MiB erlaubt).");
        }
        String extension = extension(filename);
        String verifiedType;
        if (startsWith(data, PDF_SIGNATURE) && hasPdfEndMarker(data)) {
            verifiedType = "application/pdf";
        } else if (isPng(data)) {
            verifiedType = "image/png";
        } else if (isJpeg(data)) {
            verifiedType = "image/jpeg";
        } else {
            throw new ApiException(400, "Ungültiger Nachweis. Erlaubt sind PDF, JPEG und PNG mit gültiger Dateisignatur.");
        }
        if (!matchesExtension(extension, verifiedType) || !matchesDeclaredType(declaredContentType, verifiedType)) {
            throw new ApiException(400, "Dateityp, Dateiendung und Dateiinhalte stimmen nicht überein.");
        }
        return verifiedType;
    }

    private static String extension(String filename) {
        if (filename == null) {
            return "";
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        int dot = lower.lastIndexOf('.');
        return dot >= 0 ? lower.substring(dot) : "";
    }

    private static boolean matchesExtension(String extension, String contentType) {
        return switch (contentType) {
            case "application/pdf" -> ".pdf".equals(extension);
            case "image/png" -> ".png".equals(extension);
            case "image/jpeg" -> ".jpg".equals(extension) || ".jpeg".equals(extension);
            default -> false;
        };
    }

    private static boolean matchesDeclaredType(String declaredContentType, String verifiedType) {
        return declaredContentType == null || declaredContentType.isBlank()
                || verifiedType.equalsIgnoreCase(declaredContentType.trim());
    }

    private static boolean isPng(byte[] data) {
        return startsWith(data, PNG_SIGNATURE) && data.length >= 24
                && data[12] == 'I' && data[13] == 'H' && data[14] == 'D' && data[15] == 'R'
                && readInt(data, 16) > 0 && readInt(data, 20) > 0;
    }

    private static boolean isJpeg(byte[] data) {
        return startsWith(data, JPEG_SIGNATURE) && data.length >= 4
                && (data[data.length - 2] & 0xFF) == 0xFF && (data[data.length - 1] & 0xFF) == 0xD9;
    }

    private static boolean hasPdfEndMarker(byte[] data) {
        int start = Math.max(0, data.length - 1024);
        byte[] marker = "%%EOF".getBytes(StandardCharsets.US_ASCII);
        for (int i = start; i <= data.length - marker.length; i++) {
            if (matchesAt(data, i, marker)) {
                return true;
            }
        }
        return false;
    }

    private static int readInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24) | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
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
