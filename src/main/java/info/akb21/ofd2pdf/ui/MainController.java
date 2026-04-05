package info.akb21.ofd2pdf.ui;

import info.akb21.ofd2pdf.model.BatchSummary;
import info.akb21.ofd2pdf.model.ConversionTask;
import info.akb21.ofd2pdf.model.TaskStatus;
import info.akb21.ofd2pdf.service.AppPreferences;
import info.akb21.ofd2pdf.service.ConversionManager;
import info.akb21.ofd2pdf.service.OutputPathResolver;
import info.akb21.ofd2pdf.util.DesktopUtils;
import info.akb21.ofd2pdf.util.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController {

    private record NamedLink(String name, String url) {}

    private static final String ABOUT_AUTHOR = "AKB21";
    private static final List<String> ABOUT_LINKS = List.of(
        "https://github.com/akb21/ofd2pdf"
    );
    private static final List<NamedLink> LICENSE_LINKS = List.of(
        new NamedLink(
            "本项目许可证（Apache License 2.0）",
            "https://www.apache.org/licenses/LICENSE-2.0"
        ),
        new NamedLink(
            "第三方许可证（ofdrw）",
            "https://github.com/ofdrw/ofdrw/blob/master/LICENSE"
        )
    );

    private final Stage stage;
    private final OutputPathResolver outputPathResolver;
    private final ConversionManager conversionManager;
    private final AppPreferences preferences;
    private final ObservableList<ConversionTask> tasks =
        FXCollections.observableArrayList();
    private final ExecutorService executor =
        Executors.newSingleThreadExecutor();
    private volatile boolean running;
    private boolean recursiveScan;
    private Path customOutputDirectory;
    private final String appVersion;
    private MainView view;

    public MainController(
        Stage stage,
        OutputPathResolver outputPathResolver,
        ConversionManager conversionManager,
        AppPreferences preferences,
        String appVersion
    ) {
        this.stage = stage;
        this.outputPathResolver = outputPathResolver;
        this.conversionManager = conversionManager;
        this.preferences = preferences;
        this.customOutputDirectory = preferences.getLastOutputDirectory();
        this.appVersion =
            appVersion == null || appVersion.isBlank() ? "dev" : appVersion;
    }

    public void attachView(MainView view) {
        this.view = view;
        refreshSummary();
        view.updateOutputDirectoryLabel(customOutputDirectory);
    }

    public ObservableList<ConversionTask> getTasks() {
        return tasks;
    }

    public void chooseFiles() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择 OFD 文件");
        chooser
            .getExtensionFilters()
            .add(new FileChooser.ExtensionFilter("OFD 文件", "*.ofd"));
        applyInitialDirectory(chooser, preferences.getLastInputDirectory());
        List<java.io.File> files = chooser.showOpenMultipleDialog(stage);
        if (files == null || files.isEmpty()) {
            return;
        }
        rememberInputDirectory(files.get(0).toPath().getParent());
        addInputPaths(files.stream().map(java.io.File::toPath).toList());
    }

    public void chooseFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择文件夹");
        applyInitialDirectory(chooser, preferences.getLastInputDirectory());
        java.io.File directory = chooser.showDialog(stage);
        if (directory == null) {
            return;
        }
        Path folder = directory.toPath();
        rememberInputDirectory(folder);
        addInputPaths(List.of(folder));
    }

    public void chooseOutputDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择输出目录");
        applyInitialDirectory(
            chooser,
            customOutputDirectory != null
                ? customOutputDirectory
                : preferences.getLastOutputDirectory()
        );
        java.io.File directory = chooser.showDialog(stage);
        if (directory == null) {
            return;
        }
        customOutputDirectory = directory.toPath().toAbsolutePath().normalize();
        preferences.setLastOutputDirectory(customOutputDirectory);
        refreshTaskTargets();
        refreshSummary();
    }

    public void resetOutputDirectory() {
        customOutputDirectory = null;
        preferences.setLastOutputDirectory(null);
        refreshTaskTargets();
        refreshSummary();
    }

    public void setRecursiveScan(boolean recursiveScan) {
        this.recursiveScan = recursiveScan;
    }

    public void addInputPaths(Collection<Path> paths) {
        Set<Path> uniqueInputs = FileUtils.uniquePaths(paths);
        if (uniqueInputs.isEmpty()) {
            return;
        }

        List<Path> ofdFiles = FileUtils.collectOfdFiles(
            uniqueInputs,
            recursiveScan
        );
        if (ofdFiles.isEmpty()) {
            showInfo(
                "未找到 OFD 文件",
                recursiveScan
                    ? "所选路径中未找到任何 .ofd 文件。"
                    : "所选路径的当前目录中未找到任何 .ofd 文件。"
            );
            return;
        }

        for (Path path : uniqueInputs) {
            if (Files.isDirectory(path)) {
                rememberInputDirectory(path);
                break;
            }
            Path parent = path.getParent();
            if (parent != null) {
                rememberInputDirectory(parent);
                break;
            }
        }
        addInputFiles(ofdFiles);
    }

    public void addInputFiles(Collection<Path> paths) {
        Set<Path> unique = FileUtils.uniquePaths(paths);
        int duplicates = 0;

        for (Path path : unique) {
            if (containsTask(path)) {
                duplicates++;
                continue;
            }
            Path pdfPath = outputPathResolver.resolvePdfPath(
                path,
                customOutputDirectory
            );
            tasks.add(new ConversionTask(path, pdfPath));
        }

        refreshSummary();
        if (duplicates > 0) {
            showInfo("重复文件", "已忽略 " + duplicates + " 个重复文件。");
        }
    }

    public void startConversion() {
        if (running) {
            return;
        }
        if (tasks.isEmpty()) {
            showInfo("没有可转换的任务", "请先添加一个或多个 .ofd 文件。");
            return;
        }

        running = true;
        updateControls();

        executor.submit(() -> {
            BatchSummary summary = conversionManager.convertAll(
                List.copyOf(tasks),
                this::confirmOverwriteOnFxThread,
                (task, update) ->
                    runOnFxThreadAndWait(() -> {
                        task.setStatus(update.status());
                        task.setMessage(update.message());
                        view.refreshTaskTable();
                        refreshSummary();
                    })
            );

            Platform.runLater(() -> {
                running = false;
                updateControls();
                refreshSummary();
                showInfo(
                    "转换完成",
                    "总数: " +
                        summary.total() +
                        "\n成功: " +
                        summary.success() +
                        "\n失败: " +
                        summary.failed() +
                        "\n跳过: " +
                        summary.skipped()
                );
            });
        });
    }

    public void openOutputDirectory() {
        Path outputDir = customOutputDirectory;
        if (outputDir == null && !tasks.isEmpty()) {
            outputDir = tasks.get(tasks.size() - 1).getTargetPath().getParent();
        }
        if (outputDir == null) {
            showInfo("没有可打开的目录", "当前还没有可打开的输出目录。");
            return;
        }
        try {
            DesktopUtils.openDirectory(outputDir);
        } catch (IOException ex) {
            showError("打开输出目录失败", ex.getMessage());
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于");
        alert.setHeaderText("OFD 转 PDF");
        alert.getButtonTypes().setAll(ButtonType.OK);

        Label authorLabel = new Label("作者：" + ABOUT_AUTHOR);
        Label versionLabel = new Label("版本：" + appVersion);
        VBox content = new VBox(8, authorLabel, versionLabel);
        content.setPadding(new Insets(4, 0, 0, 0));
        if (!ABOUT_LINKS.isEmpty()) {
            String firstLink = ABOUT_LINKS.get(0);
            Hyperlink firstHyperlink = new Hyperlink(firstLink);
            firstHyperlink.setOnAction(event -> openLink(firstLink));
            HBox firstLinkRow = new HBox(
                6,
                new Label("链接："),
                firstHyperlink
            );
            firstLinkRow.setAlignment(Pos.BASELINE_LEFT);
            content.getChildren().add(firstLinkRow);

            for (int i = 1; i < ABOUT_LINKS.size(); i++) {
                String link = ABOUT_LINKS.get(i);
                Hyperlink hyperlink = new Hyperlink(link);
                hyperlink.setOnAction(event -> openLink(link));
                content.getChildren().add(hyperlink);
            }
        }
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    public void showLicenseDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("许可");
        alert.setHeaderText("许可证与第三方许可证");
        alert.getButtonTypes().setAll(ButtonType.OK);

        VBox content = new VBox(8);
        content.setPadding(new Insets(4, 0, 0, 0));
        for (NamedLink link : LICENSE_LINKS) {
            Hyperlink hyperlink = new Hyperlink(
                link.name() + "： " + link.url()
            );
            hyperlink.setOnAction(event -> openLink(link.url()));
            content.getChildren().add(hyperlink);
        }
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    private void openLink(String url) {
        try {
            DesktopUtils.browseLink(url);
        } catch (IOException ex) {
            showError("打开链接失败", ex.getMessage());
        }
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private boolean containsTask(Path sourcePath) {
        Path normalized = sourcePath.toAbsolutePath().normalize();
        return tasks
            .stream()
            .anyMatch(task ->
                task
                    .getSourcePath()
                    .toAbsolutePath()
                    .normalize()
                    .equals(normalized)
            );
    }

    private boolean confirmOverwriteOnFxThread(ConversionTask task) {
        final boolean[] result = new boolean[1];
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("覆盖已有 PDF");
            alert.setHeaderText("目标 PDF 文件已存在");
            alert.setContentText(
                task.getTargetPath().toAbsolutePath().toString()
            );
            result[0] = alert
                .showAndWait()
                .filter(ButtonType.OK::equals)
                .isPresent();
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
        return result[0];
    }

    private void runOnFxThreadAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void refreshSummary() {
        if (view == null) {
            return;
        }
        long success = tasks
            .stream()
            .filter(t -> "成功".equals(t.statusProperty().get()))
            .count();
        long failed = tasks
            .stream()
            .filter(t -> "失败".equals(t.statusProperty().get()))
            .count();
        long skipped = tasks
            .stream()
            .filter(t -> "已跳过".equals(t.statusProperty().get()))
            .count();
        view.updateSummary(
            tasks.size(),
            (int) success,
            (int) failed,
            (int) skipped
        );
        view.updateOutputDirectoryLabel(customOutputDirectory);
        updateControls();
    }

    private void updateControls() {
        if (view != null) {
            view.setRunning(running);
        }
    }

    private void refreshTaskTargets() {
        tasks
            .stream()
            .filter(task -> task.getTaskStatus() == TaskStatus.PENDING)
            .forEach(task ->
                task.setTargetPath(
                    outputPathResolver.resolvePdfPath(
                        task.getSourcePath(),
                        customOutputDirectory
                    )
                )
            );
    }

    private void rememberInputDirectory(Path directory) {
        if (directory != null) {
            preferences.setLastInputDirectory(directory);
        }
    }

    private void applyInitialDirectory(FileChooser chooser, Path directory) {
        if (directory == null) {
            return;
        }
        java.io.File file = directory.toFile();
        if (file.exists() && file.isDirectory()) {
            chooser.setInitialDirectory(file);
        }
    }

    private void applyInitialDirectory(
        DirectoryChooser chooser,
        Path directory
    ) {
        if (directory == null) {
            return;
        }
        java.io.File file = directory.toFile();
        if (file.exists() && file.isDirectory()) {
            chooser.setInitialDirectory(file);
        }
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content == null ? "未知错误" : content);
        alert.showAndWait();
    }
}
