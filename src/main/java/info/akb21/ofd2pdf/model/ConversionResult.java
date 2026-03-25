package info.akb21.ofd2pdf.model;

import java.nio.file.Path;

public record ConversionResult(
        Path sourcePath,
        Path targetPath,
        TaskStatus status,
        String message,
        long durationMillis
) {
}
