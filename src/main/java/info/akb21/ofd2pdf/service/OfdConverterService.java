package info.akb21.ofd2pdf.service;

import info.akb21.ofd2pdf.model.ConversionResult;
import java.nio.file.Path;

public interface OfdConverterService {
    ConversionResult convert(Path ofdPath, Path pdfPath, boolean overwrite);
}
