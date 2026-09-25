package at.papo.partnerportal.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UploadLimits {

    public static final long IMPORT_FILE_BYTES = 2L * 1024 * 1024;
    public static final long PRODUCT_IMAGE_BYTES = 2L * 1024 * 1024;

    private UploadLimits() {
    }

    public static void rejectIfLarger(Path path, long maxBytes, String message) throws IOException {
        if (Files.size(path) > maxBytes) {
            throw new ApiException(413, message);
        }
    }
}
