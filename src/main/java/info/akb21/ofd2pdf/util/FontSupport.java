package info.akb21.ofd2pdf.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.ofdrw.converter.FontLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FontSupport {
    private static final Logger log = LoggerFactory.getLogger(FontSupport.class);
    private static volatile boolean initialized;

    private FontSupport() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        FontLoader loader = FontLoader.getInstance();
        FontLoader.setSimilarFontReplace(true);

        scanCommonFontDirs(loader);
        if (isWindows()) {
            installWindowsMappings(loader);
        } else {
            installNonWindowsFallbackMappings(loader);
        }
        chooseDefaultFont(loader);

        initialized = true;
    }

    private static void scanCommonFontDirs(FontLoader loader) {
        String home = System.getProperty("user.home");
        List<Path> dirs = isWindows()
                ? List.of(
                        Path.of("C:/Windows/Fonts"),
                        Path.of(home, "AppData", "Local", "Microsoft", "Windows", "Fonts")
                )
                : List.of(
                        Path.of("/System/Library/Fonts"),
                        Path.of("/System/Library/Fonts/Supplemental"),
                        Path.of("/System/Library/AssetsV2/com_apple_MobileAsset_Font7"),
                        Path.of("/Library/Fonts"),
                        Path.of(home, "Library", "Fonts"),
                        Path.of("/usr/share/fonts"),
                        Path.of("/usr/local/share/fonts"),
                        Path.of(home, ".fonts"),
                        Path.of(home, ".local", "share", "fonts")
                );

        for (Path dir : dirs) {
            if (Files.isDirectory(dir)) {
                try {
                    loader.scanFontDir(dir);
                    log.info("Scanned font directory: {}", dir);
                } catch (Exception ex) {
                    log.warn("Failed to scan font directory: {}", dir, ex);
                }
            }
        }
    }

    private static void installWindowsMappings(FontLoader loader) {
        mapNamedFont(loader, "宋体", "simsun.ttc", "SimSun", "NSimSun");
        mapNamedFont(loader, "SimSun", "simsun.ttc", "SimSun", "NSimSun");
        mapNamedFont(loader, "NSimSun", "simsun.ttc", "NSimSun", "SimSun");

        mapNamedFont(loader, "黑体", "simhei.ttf", "SimHei", "Microsoft YaHei");
        mapNamedFont(loader, "SimHei", "simhei.ttf", "SimHei", "Microsoft YaHei");

        mapNamedFont(loader, "微软雅黑", "msyh.ttc", "Microsoft YaHei", "SimHei");
        mapNamedFont(loader, "Microsoft YaHei", "msyh.ttc", "Microsoft YaHei", "SimHei");
        mapNamedFont(loader, "微软雅黑 Light", "msyhl.ttc", "Microsoft YaHei", "SimHei");

        mapNamedFont(loader, "楷体", "simkai.ttf", "KaiTi", "KaiTi_GB2312", "SimSun");
        mapNamedFont(loader, "KaiTi", "simkai.ttf", "KaiTi", "KaiTi_GB2312", "SimSun");
        mapNamedFont(loader, "KaiTi_GB2312", "simkai.ttf", "KaiTi_GB2312", "KaiTi", "SimSun");

        mapNamedFont(loader, "仿宋", "simfang.ttf", "FangSong", "FangSong_GB2312", "SimSun");
        mapNamedFont(loader, "FangSong", "simfang.ttf", "FangSong", "FangSong_GB2312", "SimSun");
        mapNamedFont(loader, "FangSong_GB2312", "simfang.ttf", "FangSong_GB2312", "FangSong", "SimSun");

        mapNamedFont(loader, "Arial", "arial.ttf", "Arial");
        mapNamedFont(loader, "ArialMT", "arial.ttf", "Arial");
        mapNamedFont(loader, "Calibri", "calibri.ttf", "Calibri", "Arial");
        mapNamedFont(loader, "Calibri Light", "calibril.ttf", "Calibri Light", "Calibri", "Arial");
        mapNamedFont(loader, "Times New Roman", "times.ttf", "Times New Roman", "SimSun");
        mapNamedFont(loader, "TimesNewRomanPSMT", "times.ttf", "Times New Roman", "SimSun");
        mapNamedFont(loader, "TimesNewRomanPS-BoldMT", "timesbd.ttf", "Times New Roman", "SimSun");
        mapNamedFont(loader, "Courier New", "cour.ttf", "Courier New", "Consolas");
        mapNamedFont(loader, "CourierNewPSMT", "cour.ttf", "Courier New", "Consolas");
        mapNamedFont(loader, "Consolas", "consola.ttf", "Consolas", "Courier New");
        mapNamedFont(loader, "Tahoma", "tahoma.ttf", "Tahoma", "Arial");
        mapNamedFont(loader, "Verdana", "verdana.ttf", "Verdana", "Arial");

        loader.addSimilarFontReplaceRegexMapping(".*SimSun.*", "SimSun");
        loader.addSimilarFontReplaceRegexMapping(".*Song.*", "SimSun");
        loader.addSimilarFontReplaceRegexMapping(".*NSimSun.*", "NSimSun");
        loader.addSimilarFontReplaceRegexMapping(".*SimHei.*", "SimHei");
        loader.addSimilarFontReplaceRegexMapping(".*Hei.*", "SimHei");
        loader.addSimilarFontReplaceRegexMapping(".*YaHei.*", "Microsoft YaHei");
        loader.addSimilarFontReplaceRegexMapping(".*Kai.*", "KaiTi");
        loader.addSimilarFontReplaceRegexMapping(".*FangSong.*", "FangSong");
        loader.addSimilarFontReplaceRegexMapping(".*Arial.*", "Arial");
        loader.addSimilarFontReplaceRegexMapping(".*Calibri.*", "Calibri");
        loader.addSimilarFontReplaceRegexMapping(".*Times.*", "Times New Roman");
        loader.addSimilarFontReplaceRegexMapping(".*Courier.*", "Courier New");
        loader.addSimilarFontReplaceRegexMapping(".*Consolas.*", "Consolas");
        loader.addSimilarFontReplaceRegexMapping(".*Tahoma.*", "Tahoma");
        loader.addSimilarFontReplaceRegexMapping(".*Verdana.*", "Verdana");
    }

    private static void installNonWindowsFallbackMappings(FontLoader loader) {
        mapNamedFont(loader, "宋体", "Songti.ttc", "Songti SC", "STSong", "Noto Serif CJK SC");
        mapNamedFont(loader, "SimSun", "Songti.ttc", "Songti SC", "STSong", "Noto Serif CJK SC");
        mapNamedFont(loader, "NSimSun", "Songti.ttc", "Songti SC", "STSong", "Noto Serif CJK SC");

        mapNamedFont(loader, "黑体", "PingFang.ttc", "PingFang SC", "Heiti SC", "STHeiti", "Noto Sans CJK SC");
        mapNamedFont(loader, "SimHei", "PingFang.ttc", "PingFang SC", "Heiti SC", "STHeiti", "Noto Sans CJK SC");
        mapNamedFont(loader, "微软雅黑", "PingFang.ttc", "PingFang SC", "Heiti SC", "Noto Sans CJK SC");
        mapNamedFont(loader, "Microsoft YaHei", "PingFang.ttc", "PingFang SC", "Heiti SC", "Noto Sans CJK SC");

        mapNamedFont(loader, "楷体", "Kaiti.ttc", "Kaiti SC", "STKaiti", "KaiTi", "Noto Serif CJK SC");
        mapNamedFont(loader, "KaiTi", "Kaiti.ttc", "Kaiti SC", "STKaiti", "Noto Serif CJK SC");
        mapNamedFont(loader, "KaiTi_GB2312", "Kaiti.ttc", "Kaiti SC", "STKaiti", "Noto Serif CJK SC");

        mapNamedFont(loader, "仿宋", "STFANGSO.ttf", "STFangsong", "FangSong", "Noto Serif CJK SC");
        mapNamedFont(loader, "FangSong", "STFANGSO.ttf", "STFangsong", "Noto Serif CJK SC");
        mapNamedFont(loader, "FangSong_GB2312", "STFANGSO.ttf", "STFangsong", "Noto Serif CJK SC");
    }

    private static void addAlias(FontLoader loader, String source, String... candidates) {
        for (String candidate : candidates) {
            String path = loader.getSystemFontPath(candidate, "");
            if (path != null && !path.isBlank()) {
                loader.addAliasMapping(source, candidate);
                return;
            }
        }
    }

    private static void mapNamedFont(FontLoader loader, String source, String fileName, String... candidates) {
        Path directPath = findFontFile(fileName);
        if (directPath != null) {
            loader.addSystemFontMapping(source, directPath.toAbsolutePath().toString());
            log.info("Mapped OFD font {} to {}", source, directPath);
            return;
        }

        for (String candidate : candidates) {
            String path = loader.getSystemFontPath(candidate, "");
            if (path != null && !path.isBlank()) {
                loader.addSystemFontMapping(source, path);
                log.info("Mapped OFD font {} to {} via {}", source, path, candidate);
                return;
            }
        }

        for (String candidate : candidates) {
            addAlias(loader, source, candidate);
        }
    }

    private static Path findFontFile(String fileName) {
        String home = System.getProperty("user.home");
        List<Path> roots = isWindows()
                ? List.of(
                        Path.of("C:/Windows/Fonts"),
                        Path.of(home, "AppData", "Local", "Microsoft", "Windows", "Fonts")
                )
                : List.of(
                        Path.of("/System/Library/Fonts"),
                        Path.of("/System/Library/Fonts/Supplemental"),
                        Path.of("/System/Library/AssetsV2/com_apple_MobileAsset_Font7"),
                        Path.of("/Library/Fonts"),
                        Path.of(home, "Library", "Fonts"),
                        Path.of("/usr/share/fonts"),
                        Path.of("/usr/local/share/fonts"),
                        Path.of(home, ".fonts"),
                        Path.of(home, ".local", "share", "fonts")
                );

        for (Path root : roots) {
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (var stream = Files.walk(root)) {
                Path match = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().equalsIgnoreCase(fileName))
                        .findFirst()
                        .orElse(null);
                if (match != null) {
                    return match;
                }
            } catch (Exception ex) {
                log.debug("Skipped font file search in {}", root, ex);
            }
        }
        return null;
    }

    private static void chooseDefaultFont(FontLoader loader) {
        List<String> candidates = isWindows()
                ? List.of(
                        "SimSun",
                        "NSimSun",
                        "Microsoft YaHei",
                        "SimHei",
                        "Arial",
                        "Calibri",
                        "Times New Roman"
                )
                : List.of(
                        "Arial Unicode MS",
                        "Arial",
                        "Helvetica",
                        "Times New Roman",
                        "PingFang SC",
                        "Songti SC",
                        "Noto Sans CJK SC",
                        "Noto Serif CJK SC",
                        "Heiti SC",
                        "STHeiti",
                        "Menlo"
                );

        for (String candidate : candidates) {
            String path = loader.getSystemFontPath(candidate, "");
            if (path != null && !path.isBlank() && FontLoader.loadAsDefaultFont(path)) {
                log.info("Using default conversion font: {} -> {}", candidate, path);
                return;
            }
        }

        log.warn("No preferred default conversion font was resolved; ofdrw fallback remains in use.");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
