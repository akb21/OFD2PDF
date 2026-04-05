package info.akb21.ofd2pdf.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
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

    public static void browseLink(String url) throws IOException {
        if (url == null || url.isBlank()) {
            return;
        }
        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Desktop integration is not supported");
        }
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IllegalArgumentException ex) {
            throw new IOException("Invalid URL: " + url, ex);
        }
    }
}
