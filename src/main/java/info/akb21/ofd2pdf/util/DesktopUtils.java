package info.akb21.ofd2pdf.util;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;

public final class DesktopUtils {
    private DesktopUtils() {
    }

    public static void openDirectory(Path path) throws IOException {
        if (path == null) {
            return;
        }
        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Desktop integration is not supported");
        }
        Desktop.getDesktop().open(path.toFile());
    }
}
