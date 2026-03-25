package info.akb21.ofd2pdf.service;

import info.akb21.ofd2pdf.model.ConversionResult;
import info.akb21.ofd2pdf.model.TaskStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.ofdrw.converter.export.OFDExporter;
import org.ofdrw.converter.export.PDFExporterIText;

public class OfdrwConverterService implements OfdConverterService {
    @Override
    public ConversionResult convert(Path ofdPath, Path pdfPath, boolean overwrite) {
        long start = System.currentTimeMillis();
        try {
            validateInput(ofdPath);
            prepareOutput(pdfPath, overwrite);

            try (OFDExporter exporter = new PDFExporterIText(ofdPath, pdfPath)) {
                exporter.export();
            }

            return new ConversionResult(ofdPath, pdfPath, TaskStatus.SUCCESS, "转换成功", elapsed(start));
        } catch (Exception ex) {
            return new ConversionResult(ofdPath, pdfPath, TaskStatus.FAILED, mapErrorMessage(ex), elapsed(start));
        }
    }

    private void validateInput(Path ofdPath) {
        if (ofdPath == null || Files.notExists(ofdPath)) {
            throw new IllegalArgumentException("源文件不存在");
        }
        if (!Files.isRegularFile(ofdPath)) {
            throw new IllegalArgumentException("源路径不是文件");
        }
    }

    private void prepareOutput(Path pdfPath, boolean overwrite) throws IOException {
        Path parent = pdfPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (Files.exists(pdfPath)) {
            if (!overwrite) {
                throw new IllegalStateException("目标文件已存在");
            }
            if (!Files.isWritable(pdfPath)) {
                throw new IllegalStateException("目标文件不可写");
            }
        }
    }

    private String mapErrorMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return "转换失败";
        }
        return message;
    }

    private long elapsed(long start) {
        return System.currentTimeMillis() - start;
    }
}
