package info.akb21.ofd2pdf.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FileUtilsTest {
    @Test
    void shouldRecognizeOfdFile() throws IOException {
        Path file = Files.createTempFile("sample", ".ofd");
        try {
            assertTrue(FileUtils.isOfdFile(file));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void shouldRejectNonOfdFile() throws IOException {
        Path file = Files.createTempFile("sample", ".txt");
        try {
            assertFalse(FileUtils.isOfdFile(file));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void shouldKeepUniquePaths() {
        Path path = Path.of("/tmp/demo.ofd");

        Set<Path> unique = FileUtils.uniquePaths(List.of(path, path, path.toAbsolutePath().normalize()));

        assertEquals(1, unique.size());
    }

    @Test
    void shouldCollectOfdFilesFromDirectoryWithoutRecursion() throws IOException {
        Path dir = Files.createTempDirectory("ofd2pdf-dir");
        Path nestedDir = Files.createDirectories(dir.resolve("nested"));
        Path rootOfd = Files.createFile(dir.resolve("root.ofd"));
        Files.createFile(nestedDir.resolve("nested.ofd"));

        List<Path> files = FileUtils.collectOfdFiles(List.of(dir), false);

        assertEquals(List.of(rootOfd.toAbsolutePath().normalize()), files);
    }

    @Test
    void shouldCollectOfdFilesFromDirectoryRecursively() throws IOException {
        Path dir = Files.createTempDirectory("ofd2pdf-dir");
        Path nestedDir = Files.createDirectories(dir.resolve("nested"));
        Path rootOfd = Files.createFile(dir.resolve("root.ofd"));
        Path nestedOfd = Files.createFile(nestedDir.resolve("nested.ofd"));

        List<Path> files = FileUtils.collectOfdFiles(List.of(dir), true);

        assertEquals(2, files.size());
        assertTrue(files.contains(rootOfd.toAbsolutePath().normalize()));
        assertTrue(files.contains(nestedOfd.toAbsolutePath().normalize()));
    }
}
