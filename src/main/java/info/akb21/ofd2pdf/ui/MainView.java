package info.akb21.ofd2pdf.ui;

import info.akb21.ofd2pdf.model.ConversionTask;
import info.akb21.ofd2pdf.model.TaskStatus;
import javafx.beans.value.ChangeListener;
import java.nio.file.Path;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class MainView {
    private final BorderPane root = new BorderPane();
    private final TableView<ConversionTask> taskTable;
    private final Button selectFilesButton = new Button("选择文件");
    private final Button selectFolderButton = new Button("选择文件夹");
    private final Button chooseOutputButton = new Button("输出目录");
    private final Button resetOutputButton = new Button("恢复同目录");
    private final Button startConversionButton = new Button("开始转换");
    private final Button openOutputButton = new Button("打开输出目录");
    private final CheckBox recursiveScanCheckBox = new CheckBox("递归扫描");
    private final Label summaryLabel = new Label();
    private final Label outputDirectoryLabel = new Label("输出目录：源文件同目录");
    private final Hyperlink aboutLink = new Hyperlink("关于");
    private final Hyperlink licenseLink = new Hyperlink("许可");

    public MainView(MainController controller) {
        controller.attachView(this);

        taskTable = createTaskTable(controller);
        StackPane dropZone = createDropZone(controller);

        styleActionButtons();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(
                10,
                selectFilesButton,
                selectFolderButton,
                recursiveScanCheckBox,
                chooseOutputButton,
                resetOutputButton,
                spacer,
                openOutputButton,
                startConversionButton
        );
        topBar.setPadding(new Insets(12, 12, 8, 12));
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox center = new VBox(12, dropZone, taskTable);
        center.setPadding(new Insets(0, 12, 12, 12));
        VBox.setVgrow(taskTable, Priority.ALWAYS);

        BorderPane bottomBar = new BorderPane();
        bottomBar.setPadding(new Insets(0, 12, 12, 12));
        bottomBar.setLeft(summaryLabel);
        HBox aboutBox = new HBox(8, outputDirectoryLabel, new Label("|"), aboutLink, licenseLink);
        aboutBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setRight(aboutBox);

        root.setTop(topBar);
        root.setCenter(center);
        root.setBottom(bottomBar);

        selectFilesButton.setOnAction(event -> controller.chooseFiles());
        selectFolderButton.setOnAction(event -> controller.chooseFolder());
        chooseOutputButton.setOnAction(event -> controller.chooseOutputDirectory());
        resetOutputButton.setOnAction(event -> controller.resetOutputDirectory());
        recursiveScanCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> controller.setRecursiveScan(newValue));
        startConversionButton.setOnAction(event -> controller.startConversion());
        openOutputButton.setOnAction(event -> controller.openOutputDirectory());
        aboutLink.setOnAction(event -> controller.showAboutDialog());
        licenseLink.setOnAction(event -> controller.showLicenseDialog());
    }

    public Parent getRoot() {
        return root;
    }

    public void updateSummary(int total, int success, int failed, int skipped) {
        summaryLabel.setText(
                "总数: " + total
                        + "    成功: " + success
                        + "    失败: " + failed
                        + "    跳过: " + skipped
        );
    }

    public void updateOutputDirectoryLabel(Path outputDirectory) {
        outputDirectoryLabel.setText(outputDirectory == null
                ? "输出目录：源文件同目录"
                : "输出目录：" + outputDirectory.toAbsolutePath());
    }

    public void refreshTaskTable() {
        taskTable.refresh();
    }

    public void setRunning(boolean running) {
        selectFilesButton.setDisable(running);
        selectFolderButton.setDisable(running);
        chooseOutputButton.setDisable(running);
        resetOutputButton.setDisable(running);
        recursiveScanCheckBox.setDisable(running);
        startConversionButton.setDisable(running);
    }

    private TableView<ConversionTask> createTaskTable(MainController controller) {
        TableView<ConversionTask> table = new TableView<>(controller.getTasks());

        TableColumn<ConversionTask, String> sourceColumn = new TableColumn<>("源文件");
        sourceColumn.setCellValueFactory(cell -> cell.getValue().sourceNameProperty());
        sourceColumn.setPrefWidth(220);
        sourceColumn.setMinWidth(180);

        TableColumn<ConversionTask, String> statusColumn = new TableColumn<>("状态");
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        statusColumn.setPrefWidth(120);
        statusColumn.setMinWidth(100);

        TableColumn<ConversionTask, String> outputColumn = new TableColumn<>("输出路径");
        outputColumn.setCellValueFactory(cell -> cell.getValue().outputPathProperty());
        outputColumn.setPrefWidth(420);
        outputColumn.setMinWidth(320);

        TableColumn<ConversionTask, String> messageColumn = new TableColumn<>("结果信息");
        messageColumn.setCellValueFactory(cell -> cell.getValue().messageProperty());
        messageColumn.setPrefWidth(180);
        messageColumn.setMinWidth(160);

        table.getColumns().setAll(List.of(sourceColumn, statusColumn, outputColumn, messageColumn));
        table.setPlaceholder(new Label("暂未添加 OFD 文件"));
        table.setRowFactory(tv -> new TableRow<>() {
            private final ChangeListener<String> statusListener = (obs, oldValue, newValue) -> applyRowStyle();
            private ConversionTask observedItem;

            {
                selectedProperty().addListener((obs, oldValue, newValue) -> applyRowStyle());
                itemProperty().addListener((obs, oldValue, newValue) -> {
                    if (oldValue != null) {
                        oldValue.statusProperty().removeListener(statusListener);
                    }
                    observedItem = newValue;
                    if (newValue != null) {
                        newValue.statusProperty().addListener(statusListener);
                    }
                    applyRowStyle();
                });
            }

            @Override
            protected void updateItem(ConversionTask item, boolean empty) {
                super.updateItem(item, empty);
                if (empty && observedItem != null) {
                    observedItem.statusProperty().removeListener(statusListener);
                    observedItem = null;
                }
                applyRowStyle();
            }

            private void applyRowStyle() {
                ConversionTask item = getItem();
                if (isEmpty() || item == null) {
                    setStyle("");
                    return;
                }

                String style = switch (item.getTaskStatus()) {
                    case SUCCESS -> "-fx-background-color: " + (isSelected() ? "#b9e7c0" : "#e8f6ea") + ";"
                            + "-fx-text-background-color: #1f2937;"
                            + "-fx-table-cell-border-color: #e5e7eb;";
                    case FAILED -> "-fx-background-color: " + (isSelected() ? "#f4b8b8" : "#fdecec") + ";"
                            + "-fx-text-background-color: #1f2937;"
                            + "-fx-table-cell-border-color: #e5e7eb;";
                    case SKIPPED -> "-fx-background-color: " + (isSelected() ? "#e2d5aa" : "#f4f1e8") + ";"
                            + "-fx-text-background-color: #1f2937;"
                            + "-fx-table-cell-border-color: #e5e7eb;";
                    default -> "";
                };

                setStyle(style);
            }
        });
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        return table;
    }

    private StackPane createDropZone(MainController controller) {
        Label label = new Label("将 .ofd 文件或文件夹拖到这里\n或点击左上角按钮导入");
        label.setTextAlignment(TextAlignment.CENTER);

        StackPane dropZone = new StackPane(label);
        dropZone.setPadding(new Insets(30));
        dropZone.setStyle(
                "-fx-background-color: #f5f7fb;"
                        + "-fx-border-color: #9aa4b2;"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-style: segments(8, 8);"
        );

        dropZone.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            List<Path> paths = event.getDragboard().getFiles().stream().map(java.io.File::toPath).toList();
            controller.addInputPaths(paths);
            event.setDropCompleted(true);
            event.consume();
        });

        return dropZone;
    }

    private void styleActionButtons() {
        startConversionButton.setPrefHeight(40);
        startConversionButton.setStyle(
                "-fx-background-color: #c75b68;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 10;"
                        + "-fx-padding: 10 16 10 16;"
        );

        openOutputButton.setPrefHeight(40);
        openOutputButton.setStyle(
                "-fx-background-color: #5f86b3;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 10;"
                        + "-fx-padding: 10 16 10 16;"
        );

        selectFilesButton.setStyle(
                "-fx-background-color: #6f8f61;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 10;"
                        + "-fx-padding: 10 16 10 16;"
        );

        selectFolderButton.setStyle(selectFilesButton.getStyle());
        chooseOutputButton.setStyle(
                "-fx-background-color: #8b6f9d;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 10;"
                        + "-fx-padding: 10 16 10 16;"
        );
        resetOutputButton.setStyle(
                "-fx-background-color: #7c7c7c;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 10;"
                        + "-fx-padding: 10 16 10 16;"
        );
    }
}
