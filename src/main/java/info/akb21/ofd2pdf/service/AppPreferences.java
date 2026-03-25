package info.akb21.ofd2pdf.service;

import java.nio.file.Path;
import java.util.prefs.Preferences;

public class AppPreferences {
    private static final String LAST_INPUT_DIRECTORY_KEY = "lastInputDirectory";
    private static final String LAST_OUTPUT_DIRECTORY_KEY = "lastOutputDirectory";
    private final Preferences preferences = Preferences.userNodeForPackage(AppPreferences.class);

    public Path getLastInputDirectory() {
        return readPath(LAST_INPUT_DIRECTORY_KEY);
    }

    public void setLastInputDirectory(Path directory) {
        writePath(LAST_INPUT_DIRECTORY_KEY, directory);
    }

    public Path getLastOutputDirectory() {
        return readPath(LAST_OUTPUT_DIRECTORY_KEY);
    }

    public void setLastOutputDirectory(Path directory) {
        writePath(LAST_OUTPUT_DIRECTORY_KEY, directory);
    }

    private Path readPath(String key) {
        String value = preferences.get(key, null);
        return value == null || value.isBlank() ? null : Path.of(value);
    }

    private void writePath(String key, Path path) {
        if (path == null) {
            preferences.remove(key);
            return;
        }
        preferences.put(key, path.toAbsolutePath().normalize().toString());
    }
}
