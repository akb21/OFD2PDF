package info.akb21.ofd2pdf.model;

import java.nio.file.Path;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ConversionTask {
    private final Path sourcePath;
    private Path targetPath;
    private TaskStatus taskStatus = TaskStatus.PENDING;
    private final StringProperty sourceName = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty(TaskStatus.PENDING.getDisplayName());
    private final StringProperty outputPath = new SimpleStringProperty();
    private final StringProperty message = new SimpleStringProperty("");

    public ConversionTask(Path sourcePath, Path targetPath) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        this.sourceName.set(sourcePath.getFileName().toString());
        this.outputPath.set(targetPath.toAbsolutePath().toString());
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public Path getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(Path targetPath) {
        this.targetPath = targetPath;
        this.outputPath.set(targetPath.toAbsolutePath().toString());
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public StringProperty sourceNameProperty() {
        return sourceName;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public StringProperty outputPathProperty() {
        return outputPath;
    }

    public StringProperty messageProperty() {
        return message;
    }

    public void setStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
        status.set(taskStatus.getDisplayName());
    }

    public void setMessage(String value) {
        message.set(value == null ? "" : value);
    }
}
