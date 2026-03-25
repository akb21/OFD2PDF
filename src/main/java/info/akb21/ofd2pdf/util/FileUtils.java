package info.akb21.ofd2pdf.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class FileUtils {
    private FileUtils() {
    }

    public static boolean isOfdFile(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            return false;
        }
        return path.getFileName().toString().toLowerCase().endsWith(".ofd");
    }

    public static Set<Path> uniquePaths(Collection<Path> input) {
        Set<Path> unique = new LinkedHashSet<>();
        for (Path path : input) {
            if (path != null) {
                unique.add(path.toAbsolutePath().normalize());
            }
        }
        return unique;
    }

    public static List<Path> collectOfdFiles(Collection<Path> input, boolean recursive) {
        List<Path> results = new ArrayList<>();
        for (Path path : uniquePaths(input)) {
            if (Files.isDirectory(path)) {
                results.addAll(scanDirectory(path, recursive));
            } else if (isOfdFile(path)) {
                results.add(path);
            }
        }
        return results;
    }

    private static List<Path> scanDirectory(Path directory, boolean recursive) {
        int maxDepth = recursive ? Integer.MAX_VALUE : 1;
        try (Stream<Path> stream = Files.walk(directory, maxDepth)) {
            return stream
                    .filter(FileUtils::isOfdFile)
                    .map(path -> path.toAbsolutePath().normalize())
                    .sorted()
                    .toList();
        } catch (Exception ex) {
            return List.of();
        }
    }
}
