package info.akb21.ofd2pdf.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class OutputPathResolverTest {
    @Test
    void shouldResolvePdfPathInSameDirectory() throws Exception {
        OutputPathResolver resolver = new OutputPathResolver();
        Path dir = Files.createTempDirectory("ofd2pdf-test");
        Path input = dir.resolve("demo.ofd");

        Path result = resolver.resolveDefaultPdfPath(input);

        assertEquals(dir.resolve("demo.pdf"), result);
    }

    @Test
    void shouldResolvePdfPathInCustomDirectory() throws Exception {
        OutputPathResolver resolver = new OutputPathResolver();
        Path sourceDir = Files.createTempDirectory("ofd2pdf-source");
        Path outputDir = Files.createTempDirectory("ofd2pdf-output");
        Path input = sourceDir.resolve("demo.ofd");

        Path result = resolver.resolvePdfPath(input, outputDir);

        assertEquals(outputDir.resolve("demo.pdf"), result);
    }
}
