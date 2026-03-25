package info.akb21.ofd2pdf.service;

import java.nio.file.Path;

public class OutputPathResolver {
    public Path resolveDefaultPdfPath(Path ofdPath) {
        return resolvePdfPath(ofdPath, null);
    }

    public Path resolvePdfPath(Path ofdPath, Path outputDirectory) {
        String fileName = ofdPath.getFileName().toString();
        int lastDot = fileName.lastIndexOf('.');
        String baseName = lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
        Path parent = outputDirectory == null
                ? ofdPath.toAbsolutePath().getParent()
                : outputDirectory.toAbsolutePath().normalize();
        return parent.resolve(baseName + ".pdf");
    }
}
